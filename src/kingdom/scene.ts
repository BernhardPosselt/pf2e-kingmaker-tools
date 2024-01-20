import {evaluateStructures, includeCapital, StructureResult, StructureStackRule} from './structures';
import {ruleSchema} from './schema';
import {CommodityStorage, Structure, structuresByName} from './data/structures';
import {Kingdom, Settlement} from './data/kingdom';
import {Activity} from './data/activities';
import {getBooleanSetting} from '../settings';
import {isNonNullable} from '../utils';
import {allSkills} from './data/skills';

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

export function parseStructureData(
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

function containsStructures(drawingPosition: ShapePosition, tokenPositions: ShapePosition[]): boolean {
    console.log(drawingPosition, tokenPositions);
    return tokenPositions.some(pos => {
        return pos.xStart >= drawingPosition.xStart
            && pos.xEnd <= drawingPosition.xEnd
            && pos.yStart >= drawingPosition.yStart
            && pos.yEnd <= drawingPosition.yEnd;
    });
}

/**
 * Go through all rectangle drawings in a scene and check if any structure token on the scene is fully inside the
 * rectangle drawing enlarged by a margin of error
 * @param scene
 */
function getFilledBlocks(scene: Scene): number {
    const gridSize = scene.grid.size;
    // increase rectangle by this many pixels to account for not placing the structure perfectly inside of it
    const marginOfErrorPx = Math.floor(0.2 * gridSize);
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
                xStart: d.x - marginOfErrorPx,
                xEnd: d.x + marginOfErrorPx + gridSize + d.width,
                yStart: d.y - marginOfErrorPx,
                yEnd: d.y + marginOfErrorPx + gridSize + d.height,
            };
            return containsStructures(drawingPosition, tokenPositions);
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

function getSceneStructures(scene: Scene): Structure[] {
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

function getSettlementStructureResult(settlement: SettlementAndScene, mode: StructureStackRule, autoCalculateSettlementLevel: boolean): StructureResult {
    const structures = getSceneStructures(settlement.scene);
    const level = getSettlementInfo(settlement, autoCalculateSettlementLevel).level;
    return evaluateStructures(structures, level, mode);
}


export function getStructureResult(mode: StructureStackRule, autoCalculateSettlementLevel: boolean, active: SettlementAndScene, capital?: SettlementAndScene): StructureResult {
    if (capital && capital.scene.id !== active.scene.id) {
        return includeCapital(getSettlementStructureResult(capital, mode, autoCalculateSettlementLevel), getSettlementStructureResult(active, mode, autoCalculateSettlementLevel));
    } else {
        return getSettlementStructureResult(active, mode, autoCalculateSettlementLevel);
    }
}

interface MergedSettlements {
    leadershipActivityNumber: number;
    settlementConsumption: number;
    storage: CommodityStorage;
    unlockedActivities: Set<Activity>;
}

export function getStructureStackMode(game: Game): StructureStackRule {
    return getBooleanSetting(game, 'kingdomAllStructureItemBonusesStack') ? 'all-structures-stack' : 'same-structures-stack';
}

export function getAllMergedSettlements(game: Game, kingdom: Kingdom): MergedSettlements {
    const mode = getStructureStackMode(game);
    const autoCalculateSettlementLevel = getBooleanSetting(game, 'autoCalculateSettlementLevel');
    return getAllSettlements(game, kingdom)
        .map(settlement => {
            const structureResult = getSettlementStructureResult(settlement, mode, autoCalculateSettlementLevel);
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
                unlockedActivities: new Set<Activity>([...prev.unlockedActivities, ...curr.unlockedActivities]),
            };
        }, {
            leadershipActivityNumber: 2,
            settlementConsumption: 0,
            storage: {ore: 0, stone: 0, luxuries: 0, lumber: 0, food: 0},
            unlockedActivities: new Set<Activity>(),
        });
}

export interface ActiveSettlementStructureResult {
    active: StructureResult;
    merged: StructureResult;
}

export function getActiveSettlementStructureResult(game: Game, kingdom: Kingdom): ActiveSettlementStructureResult | undefined {
    const activeSettlement = getSettlement(game, kingdom, kingdom.activeSettlement);
    const capitalSettlement = getCapitalSettlement(game, kingdom);
    const autoCalculateSettlementLevel = getBooleanSetting(game, 'autoCalculateSettlementLevel');
    if (activeSettlement) {
        const mode = getStructureStackMode(game);
        const activeSettlementStructures = getStructureResult(mode, autoCalculateSettlementLevel, activeSettlement);
        const mergedSettlementStructures = getStructureResult(mode, autoCalculateSettlementLevel, activeSettlement, capitalSettlement);
        return {
            active: activeSettlementStructures,
            merged: mergedSettlementStructures,
        };
    }
}

export function getSettlementsWithoutLandBorders(game: Game, kingdom: Kingdom): number {
    const mode = getStructureStackMode(game);
    const autoCalculateSettlementLevel = getBooleanSetting(game, 'autoCalculateSettlementLevel');
    return getAllSettlements(game, kingdom)
        .filter(settlementAndScene => {
            const structures = getStructureResult(mode, autoCalculateSettlementLevel, settlementAndScene);
            return (settlementAndScene.settlement?.waterBorders ?? 0) >= 4 && !structures.hasBridge;
        })
        .length;
}
