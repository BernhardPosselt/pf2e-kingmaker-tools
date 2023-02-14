import {getStringSetting, setSetting} from '../settings';
import {
    allCompanions,
    allFameTypes,
    BonusFeat,
    Commodities,
    Feat,
    getControlDC,
    getDefaultKingdomData,
    getLevelData,
    getSizeData,
    Kingdom,
    Leaders,
    LeaderValues, ResourceDieSize,
    Ruin,
    WorkSites,
} from './data';
import {capitalize, unpackFormArray, unslugifyAction} from '../utils';
import {calculateAbilityModifier, calculateInvestedBonus, calculateSkills, isInvested} from './skills';
import {Storage} from '../structures/structures';
import {AbilityScores, allArmyActivities, allLeadershipActivities, allRegionActivities} from '../actions-and-skills';
import {
    getAllSettlementSceneDataAndStructures,
    getAllSettlementSceneData,
    SettlementSceneData,
} from '../structures/scene';
import {allFeatsByName} from './feats';

export function getKingdom(game: Game): Kingdom {
    const kingdomString = getStringSetting(game, 'kingdom');
    return kingdomString === '' ? getDefaultKingdomData() : JSON.parse(kingdomString);
}

export async function saveKingdom(game: Game, kingdom: Kingdom): Promise<void> {
    return await setSetting(game, 'kingdom', JSON.stringify(kingdom));
}

interface KingdomOptions {
    game: Game;
}

type KingdomTabs = 'status' | 'skills' | 'turn' | 'feats' | 'trade-agreements';

const levels = [...Array.from(Array(20).keys()).map(k => k + 1)];

class KingdomApp extends FormApplication<FormApplicationOptions & KingdomOptions, object, null> {
    static override get defaultOptions(): FormApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'kingdom-app';
        options.title = 'Kingdom';
        options.template = 'modules/pf2e-kingmaker-tools/templates/kingdom/sheet.hbs';
        options.submitOnChange = true;
        options.closeOnSubmit = false;
        options.classes = ['kingmaker-tools-app', 'kingdom-app'];
        options.width = 800;
        options.height = 'auto';
        options.scrollY = ['.km-content', '.km-sidebar'];
        return options;
    }

    private readonly game: Game;
    private nav: KingdomTabs = 'trade-agreements';

    constructor(object: null, options: Partial<FormApplicationOptions> & KingdomOptions) {
        super(object, options);
        this.game = options.game;
    }

    private readKingdomData(): Kingdom {
        return getDefaultKingdomData();
    }

    override getData(options?: Partial<FormApplicationOptions>): object {
        const isGM = this.game.user?.isGM ?? false;
        const kingdomData = this.readKingdomData();
        const levelData = getLevelData(kingdomData.level);
        const sizeData = getSizeData(kingdomData.size);
        const allSettlementSceneData = getAllSettlementSceneData(this.game);
        const settlementSceneDataAndStructures = getAllSettlementSceneDataAndStructures(this.game);
        const {
            leadershipActivityNumber,
            settlementConsumption,
            storage,
        } = this.getSettlementData(settlementSceneDataAndStructures);
        const totalConsumption = kingdomData.consumption.armies + kingdomData.consumption.other + settlementConsumption;
        return {
            ...super.getData(options),
            isGM,
            isUser: !isGM,
            leadershipActivityNumber: leadershipActivityNumber,
            name: kingdomData.name,
            size: kingdomData.size,
            xp: kingdomData.xp,
            xpThreshold: kingdomData.xpThreshold,
            level: kingdomData.level,
            fame: kingdomData.fame,
            fameType: kingdomData.fameType,
            charter: kingdomData.charter,
            heartland: kingdomData.heartland,
            government: kingdomData.government,
            type: capitalize(sizeData.type),
            controlDC: getControlDC(kingdomData.level, kingdomData.size),
            atWar: kingdomData.atWar,
            unrest: kingdomData.unrest,
            resourceDieSize: sizeData.resourceDieSize,
            resourceDice: levelData.resourceDice,
            resources: kingdomData.resources,
            resourcesNextRound: kingdomData.resourcesNextRound,
            consumption: kingdomData.consumption,
            activeSettlement: kingdomData.activeSettlement,
            levels,
            settlementConsumption,
            totalConsumption,
            ruin: this.getRuin(kingdomData.ruin),
            commodities: this.getCommodities(
                kingdomData.commodities,
                kingdomData.commoditiesNextRound,
                sizeData.commodityStorage,
                storage
            ),
            workSites: this.getWorkSites(kingdomData.workSites),
            ...this.getActiveTabs(),
            skills: calculateSkills({
                ruin: kingdomData.ruin,
                skillRanks: kingdomData.skillRanks,
                leaders: kingdomData.leaders,
                abilityScores: kingdomData.abilityScores,
                unrest: kingdomData.unrest,
                kingdomLevel: kingdomData.level,
            }),
            leaders: this.getLeaders(kingdomData.leaders),
            abilities: this.getAbilities(kingdomData.abilityScores, kingdomData.leaders, kingdomData.level),
            fameTypes: allFameTypes,
            fameLabel: kingdomData.fameType === 'famous' ? 'Fame' : 'Infamy',
            tradeAgreements: kingdomData.tradeAgreements,
            ...this.getFeats(kingdomData.feats, kingdomData.bonusFeats, kingdomData.level),
            tradeAgreementsSize: kingdomData.tradeAgreements.filter(t => t.relations === 'trade-agreement').length,
            ranks: [
                {label: 'Untrained', value: 0},
                {label: 'Trained', value: 1},
                {label: 'Expert', value: 2},
                {label: 'Master', value: 3},
                {label: 'Legendary', value: 4},
            ],
            terrains: [
                {label: 'Swamp', value: 'swamp'},
                {label: 'Hills', value: 'hills'},
                {label: 'Plains', value: 'plains'},
                {label: 'Mountains', value: 'mountains'},
                {label: 'Forest', value: 'forest'},
            ],
            actorTypes: [
                {label: 'PC', value: 'pc'},
                {label: 'NPC', value: 'npc'},
                {label: 'Companion', value: 'companion'},
            ],
            companions: allCompanions,
            settlements: allSettlementSceneData,
            tradeAgreementRelationTypes: [
                {label: 'None', value: 'none'},
                {label: 'Diplomatic Relations', value: 'diplomatic-relations'},
                {label: 'Trade Agreement', value: 'trade-agreement'},
            ],
            ongoingEvents: kingdomData.ongoingEvents,
            // TODO: allow milestone home brewing and sort by xp => name
            milestones: kingdomData.milestones,
            // TODO: filter out companion activities if not in position of leader
            leadershipActivities: allLeadershipActivities.map(a => {
                return {label: unslugifyAction(a), value: a};
            }),
            regionActivities: allRegionActivities.map(a => {
                return {label: unslugifyAction(a), value: a};
            }),
            armyActivities: allArmyActivities.map(a => {
                return {label: unslugifyAction(a), value: a};
            }),
            canLevelUp: kingdomData.xp >= kingdomData.xpThreshold && kingdomData.level < 20,
        };
    }

    private getActiveTabs(): object {
        return {
            statusTab: this.nav === 'status',
            skillsTab: this.nav === 'skills',
            turnTab: this.nav === 'turn',
            tradeAgreementsTab: this.nav === 'trade-agreements',
            featsTab: this.nav === 'feats',
        };
    }

    /* eslint-disable @typescript-eslint/no-explicit-any */
    override async _updateObject(event: Event, formData: any): Promise<void> {
        console.log(formData);
        const kingdom = expandObject(formData);
        kingdom.tradeAgreements = unpackFormArray(kingdom.tradeAgreements);
        kingdom.feats = unpackFormArray(kingdom.feats);
        kingdom.bonusFeats = unpackFormArray(kingdom.bonusFeats);
        kingdom.milestones = unpackFormArray(kingdom.milestones);
        // kingdom.ongoingEvents = unpackFormArray(kingdom.ongoingEvents);
        console.log(kingdom);
        // await saveKingdom(this.game, formData);
        this.render();
    }

    private async update(data: Partial<Kingdom>): Promise<void> {
        console.log('Update', data);
        this.render();
    }

    public sceneChange(): void {
        this.render();
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        Hooks.on('canvasReady', this.sceneChange.bind(this));
        Hooks.on('createToken', this.sceneChange.bind(this));
        Hooks.on('deleteToken', this.sceneChange.bind(this));
        const $html = html[0];
        $html.querySelectorAll('.km-nav a')?.forEach(el => {
            el.addEventListener('click', (event) => {
                const tab = event.target as HTMLAnchorElement;
                this.nav = tab.dataset.tab as KingdomTabs;
                this.render();
            });
        });
        $html.querySelector('#km-gain-fame')
            ?.addEventListener('click', async () => await this.update({fame: this.readKingdomData().fame + 1}));
        $html.querySelector('#km-adjust-unrest')
            ?.addEventListener('click', async () => {
                console.warn('adjusting unrest');
            });
        $html.querySelector('#km-collect-resources')
            ?.addEventListener('click', async () => await this.collectResources());
        $html.querySelector('#km-reduce-unrest')
            ?.addEventListener('click', async () => await this.reduceUnrest());
        $html.querySelector('#km-pay-consumption')
            ?.addEventListener('click', async () => {
                console.warn('paying consumption');
            });
        $html.querySelector('#km-add-event')
            ?.addEventListener('click', async () => {
                console.warn('adding event');
            });
        // TODO: delete event
        $html.querySelector('#km-resolve-event-xp')
            ?.addEventListener('click', async () => {
                console.warn('adding event xp');
            });
        $html.querySelector('#km-claimed-hexes-xp')
            ?.addEventListener('click', async () => {
                console.warn('adding hex xp');
            });
        $html.querySelector('#km-rp-to-xp')
            ?.addEventListener('click', async () => {
                console.warn('adding rp xp');
            });
        $html.querySelector('#km-leve-up')
            ?.addEventListener('click', async () => {
                const current = this.readKingdomData();
                if (current.xp >= current.xpThreshold) {
                    await this.update({level: current.level + 1, xp: current.xp - current.xpThreshold});
                } else {
                    ui.notifications?.error('Can not level up, not enough XP');
                }
            });
    }

    private async collectResources(): Promise<void> {
        const current = this.readKingdomData();
        const sizeData = getSizeData(current.size);
        const featDice = 0; // FIXME: feat?
        const settlementSceneDataAndStructures = getAllSettlementSceneDataAndStructures(this.game);
        const {storage} = this.getSettlementData(settlementSceneDataAndStructures);
        const capacity = this.calculateStorageCapacity(sizeData.commodityStorage, storage);
        const dice = current.resources.bonusResourceDice + featDice;
        const resourcePoints = await this.rollResourceDice(sizeData.resourceDieSize, dice);
        await this.update({
            resources: {
                bonusResourceDice: 0,
                resourcePoints,
            },
            commodities: this.calculateCommoditiesThisTurn(capacity, current),
            commoditiesNextRound: {
                food: 0,
                ore: 0,
                lumber: 0,
                luxuries: 0,
                stone: 0,
            },
        });
    }

    private calculateCommoditiesThisTurn(
        capacity: Commodities,
        kingdom: Kingdom,
    ): Commodities {
        const next = kingdom.commoditiesNextRound;
        const sites = kingdom.workSites;
        return {
            food: Math.max(capacity.food, next.food),
            ore: Math.max(capacity.ore, next.ore + sites.mines.quantity + sites.mines.resources),
            lumber: Math.max(capacity.lumber, next.lumber + sites.mines.quantity + sites.mines.resources),
            luxuries: Math.max(capacity.luxuries, next.luxuries + sites.luxurySources.quantity + sites.luxurySources.resources),
            stone: Math.max(capacity.stone, next.stone + sites.lumberCamps.quantity + sites.lumberCamps.resources),
        };
    }

    override close(options?: FormApplication.CloseOptions): Promise<void> {
        Hooks.off('canvasReady', this.sceneChange);
        Hooks.off('createToken', this.sceneChange);
        Hooks.off('deleteToken', this.sceneChange);
        return super.close(options);
    }

    private getRuin(ruin: Ruin): object {
        return Object.fromEntries(Object.entries(ruin)
            .map(([ruin, values]) => [ruin, {label: capitalize(ruin), ...values}])
        );
    }

    private getWorkSites(workSites: WorkSites): object {
        return Object.fromEntries(Object.entries(workSites)
            .map(([key, values]) => {
                const label = key === 'lumberCamps' ? 'Lumber Camps' : (key === 'luxurySources' ? 'Luxury Sources' : capitalize(key));
                return [key, {label: label, ...values}];
            })
        );
    }

    private calculateStorageCapacity(capacity: number, storage: Storage): Commodities {
        return {
            food: capacity + storage.food,
            ore: capacity + storage.ore,
            luxuries: capacity + storage.luxuries,
            lumber: capacity + storage.lumber,
            stone: capacity + storage.stone,
        };
    }

    private getCommodities(
        commodities: Commodities,
        commoditiesNextRound: Commodities,
        capacity: number,
        storage: Storage,
    ): object {
        const storageCapacity = this.calculateStorageCapacity(capacity, storage);
        return Object.fromEntries((Object.entries(commodities) as [keyof Commodities, number][])
            .map(([commodity, value]) => [commodity, {
                label: capitalize(commodity),
                value: value,
                capacity: storageCapacity[commodity],
                next: commoditiesNextRound[commodity],
            }])
        );
    }

    private getLeaders(leaders: Leaders): object {
        return Object.fromEntries((Object.entries(leaders) as [keyof Leaders, LeaderValues][])
            .map(([leader, values]) => {
                return [leader, {
                    label: capitalize(leader),
                    isCompanion: values.type === 'companion',
                    ...values,
                }];
            }));
    }

    private getAbilities(abilityScores: AbilityScores, leaders: Leaders, kingdomLevel: number): object {
        return Object.fromEntries((Object.entries(abilityScores) as [keyof AbilityScores, number][])
            .map(([ability, score]) => {
                return [ability, {
                    label: capitalize(ability),
                    score: score,
                    modifier: calculateAbilityModifier(score),
                    invested: isInvested(ability, leaders),
                    investedBonus: calculateInvestedBonus(kingdomLevel, ability, leaders),
                }];
            }));
    }

    private getFeats(feats: Feat[], bonusFeats: BonusFeat[], kingdomLevel: number): object {
        const levelFeats = [];
        const takenFeatsByLevel = Object.fromEntries(feats.map(feat => [feat.level, feat]));
        const noFeat = allFeatsByName['-'];
        for (let featLevel = 2; featLevel <= kingdomLevel; featLevel += 2) {
            const existingFeat = takenFeatsByLevel[featLevel];
            if (existingFeat && existingFeat.id in allFeatsByName) {
                levelFeats.push({...allFeatsByName[existingFeat.id], takenAt: featLevel});
            } else {
                levelFeats.push({...noFeat, takenAt: featLevel});
            }
        }
        return {
            featIds: Object.keys(allFeatsByName),
            levelFeats: levelFeats,
            bonusFeats: bonusFeats
                .filter(feat => feat.id in allFeatsByName)
                .map(feat => allFeatsByName[feat.id]),
        };
    }

    private getSettlementData(settlements: SettlementSceneData[]):
        { leadershipActivityNumber: number; settlementConsumption: number; storage: Storage } {
        return settlements
            .map(settlement => {
                return {
                    leadershipActivityNumber: settlement.settlement.leadershipActivityBonus ? 3 : 2,
                    settlementConsumption: settlement.settlement.consumption,
                    storage: settlement.settlement.storage,
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
                };
            }, {
                leadershipActivityNumber: 2,
                settlementConsumption: 0,
                storage: {ore: 0, stone: 0, luxuries: 0, lumber: 0, food: 0},
            });

    }

    private async rollResourceDice(resourceDieSize: ResourceDieSize, dice: number): Promise<number> {
        const roll = await (new Roll(dice + resourceDieSize).roll());
        await roll.toMessage({flavor: 'Rolling Resource Dice'});
        return roll.total;
    }

    private async reduceUnrest(): Promise<void> {
        const roll = await (new Roll('1d20').roll());
        await roll.toMessage({flavor: 'Reducing Unrest by 1 on an 11 or higher'});
        if (roll.total > 10) {
            await this.update({
                unrest: Math.max(0, this.readKingdomData().unrest - 1),
            });
        }
    }
}

export async function showKingdom(game: Game): Promise<void> {
    new KingdomApp(null, {game}).render(true);
}
