import {evaluateStructures, includeCapital, StructureResult, StructureStackRule} from './structures';
import {ruleSchema} from './schema';
import {CommodityStorage, Structure, structuresByName} from './data/structures';
import {Kingdom, Settlement, WorkSite, WorkSites} from './data/kingdom';
import {getBooleanSetting} from '../settings';
import {isKingmakerInstalled, isNonNullable} from '../utils';
import {allSkills} from './data/skills';
import {getKingdomActivitiesById, KingdomActivityById} from './data/activityData';

class StructureError extends Error {
}

function calculateLots(structure: Structure, tokenWidth: number, tokenHeight: number): Structure {
    if (structure.lots !== undefined) {
        return structure;
    } else {
        return {
            ...structure,
            lots: tokenWidth * tokenHeight,
        };
    }
}

function parseStructureData(
    name: string | null,
    data: unknown,
    tokenWidth: number,
    tokenHeight: number,
    level: number | null,
): Structure | undefined {
    if (data === undefined || data === null) {
        return undefined;
    } else if (typeof data === 'object' && 'ref' in data) {
        const refData = data as { ref: string };
        const lookedUpStructure = structuresByName.get(refData.ref);
        if (lookedUpStructure === undefined) {
            console.log(refData, structuresByName);
            throw new StructureError(`No predefined structure data found for actor with name ${name}, aborting`);
        }
        return calculateLots(lookedUpStructure, tokenWidth, tokenHeight);
    } else if (isNonNullable(name) && isNonNullable(level)) {
        const rule: Structure = {
            name,
            level,
            // migrate older structures
            construction: {
                skills: allSkills.map(s => {
                    return {skill: s};
                }),
                dc: 0,
                rp: 0,
            },
            ...data,
        };
        const result = ruleSchema.validate(rule);
        if (result.error) {
            console.error(`Failed to validate structure with name ${name}`, data);
            console.error('Validation Error', result.error);
            throw new StructureError(`Structure with name ${name} failed to validate, aborting. See console log (F12) for more details`);
        }
        return calculateLots(rule, tokenWidth, tokenHeight);
    } else {
        const result: Structure = data as Structure;
        return calculateLots(result, tokenWidth, tokenHeight);
    }
}

export interface ActorStructure extends Structure {
    actor: Actor;
}

export function getSceneActorStructures(scene: Scene): ActorStructure[] {
    const actors = scene.tokens
        .map(t => t.actor)
        .filter(a => isNonNullable(a)) as Actor[];
    return getStructuresFromActors(actors);
}

export function getStructureFromActor(actor: Actor): ActorStructure | null {
    const width = actor.token?.width ?? actor.prototypeToken?.width ?? 0;
    const height = actor.token?.height ?? actor.prototypeToken?.height ?? 0;
    const data = parseStructureData(
        actor!.name,
        actor!.getFlag('pf2e-kingmaker-tools', 'structureData'),
        width,
        height,
        actor.level,
    );
    if (data) {
        return {
            ...data,
            actor,
        };
    }
    return null;
}

export function getStructuresFromActors(actors: Actor[]): ActorStructure[] {
    return actors
        .map((actor) => getStructureFromActor(actor))
        .filter(actor => actor !== null)! as ActorStructure[];
}

export function isStructureActor(actor: Actor): boolean {
    return isNonNullable(actor.getFlag('pf2e-kingmaker-tools', 'structureData'));
}

export function isStructureActorActive(actor: Actor): boolean {
    return actor.itemTypes.condition?.find(c => c.type === 'condition' && c.name.startsWith('Slowed')) === undefined;
}

function getBlockTiles(scene: Scene): TileDocument[] {
    return scene.tiles.filter(d => isNonNullable(d.getFlag('pf2e-kingmaker-tools', 'settlementBlockDrawing')));
}

function tokenIsStructure(token: TokenDocument): boolean {
    return token.actor !== null
        && token.actor !== undefined
        && isStructureActor(token.actor)
        && isStructureActorActive(token.actor);
}

interface ShapePosition {
    xStart: number;
    yStart: number;
    xEnd: number;
    yEnd: number;
}

function shapeContainsOtherShape(a: ShapePosition, b: ShapePosition, marginOfErrorPx: number): boolean {
    return b.xStart >= (a.xStart - marginOfErrorPx)
        && b.xEnd <= (a.xEnd + marginOfErrorPx)
        && b.yStart >= (a.yStart - marginOfErrorPx)
        && b.yEnd <= (a.yEnd + marginOfErrorPx);
}

function containsStructures(drawingPosition: ShapePosition, tokenPositions: ShapePosition[], marginOfErrorPx: number): boolean {
    return tokenPositions.some(token => shapeContainsOtherShape(drawingPosition, token, marginOfErrorPx));
}

/**
 * Go through all rectangle drawings in a scene and check if any structure token on the scene is fully inside the
 * rectangle drawing enlarged by a margin of error
 * @param scene
 */
function getFilledBlocks(scene: Scene): number {
    const gridSize = scene.grid.size;
    // increase rectangle by this many pixels to account for not placing the structure perfectly inside of it
    const marginOfErrorPx = Math.floor(0.1 * gridSize);
    const tokenPositions = scene.tokens
        .filter(tokenIsStructure)
        .map(token => {
            return {
                xStart: token.x,
                yStart: token.y,
                xEnd: token.x + (token.width * gridSize),
                yEnd: token.y + (token.height * gridSize),
            };
        });
    return getBlockTiles(scene)
        .filter(d => {
            // (0, 0) is the top left corner and x and y of the drawing is the top left corner of the drawing
            const drawingPosition = {
                xStart: d.x,
                xEnd: d.x + d.width,
                yStart: d.y,
                yEnd: d.y + d.height,
            };
            return containsStructures(drawingPosition, tokenPositions, marginOfErrorPx);
        })
        .length || 1;
}

export function getSettlementInfo(settlement: SettlementAndScene, autoCalculateSettlementLevel: boolean): {
    level: number;
    lots: number
} {
    if (autoCalculateSettlementLevel && !settlement.settlement.manualSettlementLevel) {
        const blocks = getFilledBlocks(settlement.scene);
        return {
            level: Math.min(20, blocks),
            lots: blocks,
        };
    } else {
        return {
            level: settlement.settlement.level,
            lots: settlement.settlement.lots,
        };
    }
}

export function getSceneStructures(scene: Scene): Structure[] {
    try {
        return scene.tokens
            .filter(tokenIsStructure)
            .map(t => {
                const result: [Actor | null, number, number] = [t.actor, t.width ?? 1, t.height ?? 1];
                return result;
            })
            .map(([actor, width, height]) =>
                parseStructureData(
                    actor!.name,
                    actor!.getFlag('pf2e-kingmaker-tools', 'structureData'),
                    width,
                    height,
                    actor!.level,
                ))
            .filter(data => data !== undefined) as Structure[] ?? [];
    } catch (e: unknown) {
        if (e instanceof StructureError) {
            ui.notifications?.error(e.message);
        } else {
            throw e;
        }
    }
    return [];
}

export function getScene(game: Game, sceneId: string): Scene | undefined {
    return game?.scenes?.find(scene => scene.id === sceneId);
}

export function getCurrentScene(game: Game): Scene | undefined {
    return game.scenes?.viewed;
}

export interface SettlementAndScene {
    settlement: Settlement;
    scene: Scene;
}

export function getCapitalSettlement(game: Game, kingdom: Kingdom): SettlementAndScene | undefined {
    const settlement = kingdom.settlements.find(settlement => settlement.type === 'capital');
    const scene = settlement ? getScene(game, settlement.sceneId) : undefined;
    if (scene && settlement) {
        return {
            scene: scene,
            settlement: settlement,
        };
    }
}

export function getSettlement(game: Game, kingdom: Kingdom, sceneId: string): SettlementAndScene | undefined {
    const settlement = kingdom.settlements.find(settlement => settlement.sceneId === sceneId);
    const scene = getScene(game, sceneId);
    if (scene && settlement) {
        return {
            settlement,
            scene,
        };
    }
}

export function getAllSettlements(game: Game, kingdom: Kingdom): SettlementAndScene[] {
    return game.scenes
        ?.map(scene => getSettlement(game, kingdom, scene.id))
        ?.filter(scene => scene !== undefined) as SettlementAndScene[] ?? [];
}

function getSettlementStructureResult(
    settlement: SettlementAndScene,
    mode: StructureStackRule,
    autoCalculateSettlementLevel: boolean,
    activities: KingdomActivityById,
): StructureResult {
    const structures = getSceneStructures(settlement.scene);
    const level = getSettlementInfo(settlement, autoCalculateSettlementLevel).level;
    return evaluateStructures(structures, level, mode, activities);
}


export function getStructureResult(
    mode: StructureStackRule,
    autoCalculateSettlementLevel: boolean,
    activities: KingdomActivityById,
    active: SettlementAndScene,
    capital?: SettlementAndScene,
): StructureResult {
    if (capital && capital.scene.id !== active.scene.id) {
        return includeCapital(
            getSettlementStructureResult(capital, mode, autoCalculateSettlementLevel, activities),
            getSettlementStructureResult(active, mode, autoCalculateSettlementLevel, activities));
    } else {
        return getSettlementStructureResult(active, mode, autoCalculateSettlementLevel, activities);
    }
}

interface MergedSettlements {
    leadershipActivityNumber: number;
    settlementConsumption: number;
    storage: CommodityStorage;
    unlockedActivities: Set<string>;
}

export function getStructureStackMode(game: Game): StructureStackRule {
    return getBooleanSetting(game, 'kingdomAllStructureItemBonusesStack') ? 'all-structures-stack' : 'same-structures-stack';
}

export function getAllMergedSettlements(game: Game, kingdom: Kingdom): MergedSettlements {
    const mode = getStructureStackMode(game);
    const activities = getKingdomActivitiesById(kingdom.homebrewActivities);
    const autoCalculateSettlementLevel = getBooleanSetting(game, 'autoCalculateSettlementLevel');
    return getAllSettlements(game, kingdom)
        .map(settlement => {
            const structureResult = getSettlementStructureResult(settlement, mode, autoCalculateSettlementLevel, activities);
            return {
                leadershipActivityNumber: structureResult.increaseLeadershipActivities ? 3 : 2,
                settlementConsumption: structureResult.consumption,
                storage: structureResult.storage,
                unlockedActivities: new Set(structureResult.unlockActivities),
            };
        })
        .reduce((prev, curr) => {
            return {
                leadershipActivityNumber: Math.max(prev.leadershipActivityNumber, curr.leadershipActivityNumber),
                settlementConsumption: prev.settlementConsumption + curr.settlementConsumption,
                storage: {
                    ore: prev.storage.ore + curr.storage.ore,
                    stone: prev.storage.stone + curr.storage.stone,
                    luxuries: prev.storage.luxuries + curr.storage.luxuries,
                    lumber: prev.storage.lumber + curr.storage.lumber,
                    food: prev.storage.food + curr.storage.food,
                },
                unlockedActivities: new Set<string>([...prev.unlockedActivities, ...curr.unlockedActivities]),
            };
        }, {
            leadershipActivityNumber: 2,
            settlementConsumption: 0,
            storage: {ore: 0, stone: 0, luxuries: 0, lumber: 0, food: 0},
            unlockedActivities: new Set<string>(),
        });
}

export interface ActiveSettlementStructureResult {
    active: StructureResult;
    merged: StructureResult;
}

export function getActiveSettlementStructureResult(game: Game, kingdom: Kingdom): ActiveSettlementStructureResult | undefined {
    const activeSettlement = getSettlement(game, kingdom, kingdom.activeSettlement);
    const capitalSettlement = getCapitalSettlement(game, kingdom);
    const activities = getKingdomActivitiesById(kingdom.homebrewActivities);
    const autoCalculateSettlementLevel = getBooleanSetting(game, 'autoCalculateSettlementLevel');
    if (activeSettlement) {
        const mode = getStructureStackMode(game);
        const activeSettlementStructures = getStructureResult(mode, autoCalculateSettlementLevel, activities, activeSettlement);
        const mergedSettlementStructures = getStructureResult(mode, autoCalculateSettlementLevel, activities, activeSettlement, capitalSettlement);
        return {
            active: activeSettlementStructures,
            merged: mergedSettlementStructures,
        };
    }
}

export function getSettlementsWithoutLandBorders(game: Game, kingdom: Kingdom): number {
    const mode = getStructureStackMode(game);
    const autoCalculateSettlementLevel = getBooleanSetting(game, 'autoCalculateSettlementLevel');
    const activities = getKingdomActivitiesById(kingdom.homebrewActivities);
    return getAllSettlements(game, kingdom)
        .filter(settlementAndScene => {
            const structures = getStructureResult(mode, autoCalculateSettlementLevel, activities, settlementAndScene);
            return (settlementAndScene.settlement?.waterBorders ?? 0) >= 4 && !structures.hasBridge;
        })
        .length;
}

export interface StolenLandsData {
    size: number;
    workSites: WorkSites;
}

function parseWorksite(claimedHexes: HexState[], type: CampType, commodity: CommodityType): WorkSite {
    return claimedHexes.filter(h => h.camp === type)
        .reduce((prev, state) => {
            const res = state.commodity === commodity ? 1 : 0;
            // mines on luxuries don't count for ore but a single luxury
            const quantity = type === 'mine' && state.commodity === 'luxuries' ? 0 : 1;
            if (commodity === 'luxuries') {
                return {
                    quantity: prev.quantity + res,
                    resources: prev.resources,
                };
            } else {
                return {
                    quantity: prev.quantity + quantity,
                    resources: prev.resources + res,
                };
            }
        }, {quantity: 0, resources: 0});
}

export function parseKingmaker(kingmaker: KingmakerState): StolenLandsData {
    const claimedHexes = Object.values(kingmaker.hexes)
        .filter(h => h.claimed);
    const farmHexes = claimedHexes.filter(h => h.features?.some(f => f.type === 'farmland'));
    const food = claimedHexes.filter(h => h.commodity === 'food');
    return {
        size: claimedHexes.length,
        workSites: {
            farmlands: {
                quantity: farmHexes.length,
                resources: food.length,
            },
            quarries: parseWorksite(claimedHexes, 'quarry', 'stone'),
            mines: parseWorksite(claimedHexes, 'mine', 'ore'),
            lumberCamps: parseWorksite(claimedHexes, 'lumber', 'lumber'),
            luxurySources: parseWorksite(claimedHexes, 'mine', 'luxuries'),
        },
    };
}

function findStolenLandsScene(game: Game, realmSceneId: string | null): Scene | undefined {
    if (isNonNullable(realmSceneId)) {
        return game.scenes?.find(s => s.id === realmSceneId);
    }
}

type RealmTileType =
    'mine'
    | 'ore'
    | 'lumber'
    | 'lumberCamp'
    | 'quarry'
    | 'stone'
    | 'claimed'
    | 'farmland'
    | 'luxury'
    | 'luxuryWorksite'
    | 'food';

interface RealmTile {
    type: RealmTileType;
}


export interface RealmTileData extends RealmTile, ShapePosition {
}

function parseTileData<T extends TileDocument | DrawingDocument>(
    tile: T,
    parseDimensions: (tile: T) => { width: number; height: number },
): RealmTileData | undefined {
    const data = tile.getFlag('pf2e-kingmaker-tools', 'realmTile') as RealmTile | null | undefined;
    if (data) {
        const {width, height} = parseDimensions(tile);
        return {
            ...data,
            xStart: tile.x,
            xEnd: tile.x + width,
            yStart: tile.y,
            yEnd: tile.y + height,
        };
    }
}

function getWorksitesInTiles(worksiteType: 'lumberCamp' | 'mine' | 'quarry' | 'luxuryWorksite', tilesInPos: RealmTileData[]): WorkSite {
    const commodityType = {
        lumberCamp: 'lumber',
        mine: 'ore',
        quarry: 'stone',
        luxuryWorksite: 'luxury',
    }[worksiteType];
    const quantity = tilesInPos.filter(t => t.type === worksiteType).length;
    return {
        quantity: quantity,
        resources: quantity > 0 ? tilesInPos.filter(t => t.type === commodityType).length : 0,
    };
}

function mergeWorksites(a: WorkSites, b: WorkSites): WorkSites {
    return {
        luxurySources: {
            resources: a.luxurySources.resources + b.luxurySources.resources,
            quantity: a.luxurySources.quantity + b.luxurySources.quantity,
        },
        lumberCamps: {
            resources: a.lumberCamps.resources + b.lumberCamps.resources,
            quantity: a.lumberCamps.quantity + b.lumberCamps.quantity,
        },
        quarries: {
            resources: a.quarries.resources + b.quarries.resources,
            quantity: a.quarries.quantity + b.quarries.quantity,
        },
        farmlands: {
            resources: a.farmlands.resources + b.farmlands.resources,
            quantity: a.farmlands.quantity + b.farmlands.quantity,
        },
        mines: {
            resources: a.mines.resources + b.mines.resources,
            quantity: a.mines.quantity + b.mines.quantity,
        },
    };
}

function emptyWorksites(): WorkSites {
    return {
        luxurySources: {resources: 0, quantity: 0},
        lumberCamps: {resources: 0, quantity: 0},
        quarries: {resources: 0, quantity: 0},
        farmlands: {resources: 0, quantity: 0},
        mines: {resources: 0, quantity: 0},
    };
}

export function parseSceneTiles(
    objects: RealmTileData[],
    marginOfErrorPx: number,
): StolenLandsData {
    const claimedPositions = objects.filter(o => o.type === 'claimed');
    // try to reduce quadratic complexity a bit
    const nonClaimedPositions = new Set(objects.filter(o => o.type !== 'claimed'));
    const worksites = claimedPositions
        .map(claimed => {
            const tilesInPos = Array.from(nonClaimedPositions)
                .filter(nonClaimed => shapeContainsOtherShape(claimed, nonClaimed, marginOfErrorPx));
            tilesInPos.forEach(t => nonClaimedPositions.delete(t));
            const farmlands = tilesInPos.filter(t => t.type === 'farmland').length;
            return {
                farmlands: {quantity: farmlands, resources: tilesInPos.filter(t => t.type === 'food').length},
                lumberCamps: getWorksitesInTiles('lumberCamp', tilesInPos),
                mines: getWorksitesInTiles('mine', tilesInPos),
                quarries: getWorksitesInTiles('quarry', tilesInPos),
                luxurySources: getWorksitesInTiles('luxuryWorksite', tilesInPos),
            };
        })
        .reduce(mergeWorksites, emptyWorksites());
    return {
        size: claimedPositions.length,
        workSites: worksites,
    };
}

function parseSceneData(game: Game, realmSceneId: string | null): StolenLandsData {
    const scene = findStolenLandsScene(game, realmSceneId);
    if (scene) {
        // increase rectangle by this many pixels to account for not placing the structure perfectly inside of it
        const marginOfErrorPx = Math.floor(0.1 * scene.grid.size);
        const objects = [
            ...scene.tiles
                .filter(t => t.visible)
                .map(t => parseTileData(t, (tile) => {
                    return {
                        width: tile.width,
                        height: tile.height,
                    };
                }))
                .filter(t => isNonNullable(t)) as RealmTileData[],
            ...scene.drawings
                .filter(t => t.visible)
                .map(t => parseTileData(t, (tile) => {
                    return {
                        width: tile.shape.width,
                        height: tile.shape.height,
                    };
                }))
                .filter(t => isNonNullable(t)) as RealmTileData[],
        ];
        return parseSceneTiles(objects, marginOfErrorPx);
    } else {
        return {
            workSites: emptyWorksites(),
            size: 0,
        };
    }
}

export type ResourceAutomationMode = 'kingmaker' | 'tileBased' | 'manual';

export function getStolenLandsData(
    game: Game,
    mode: ResourceAutomationMode,
    kingdom: Kingdom,
): StolenLandsData {
    if (mode === 'kingmaker' && isKingmakerInstalled(game)) {
        return parseKingmaker(kingmaker.state);
    } else if (mode === 'tileBased') {
        return parseSceneData(game, kingdom.realmSceneId);
    } else {
        return {
            size: kingdom.size,
            workSites: kingdom.workSites,
        };
    }
}


export async function makeResourceTileOrDrawing(tile: TileDocument, type: RealmTileType): Promise<void> {
    await tile.setFlag('pf2e-kingmaker-tools', 'realmTile', {type});
}

export async function makeResourceDrawing(tile: DrawingDocument, type: RealmTileType): Promise<void> {
    await tile.setFlag('pf2e-kingmaker-tools', 'realmTile', {type});
}