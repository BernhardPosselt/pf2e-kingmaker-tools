import {getBooleanSetting, getStringSetting} from '../settings';
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
    KingdomSizeData,
    Leaders,
    LeaderValues,
    ResourceDieSize,
    Ruin,
    WorkSites,
} from './data';
import {capitalize, unpackFormArray, unslugifyAction} from '../utils';
import {calculateAbilityModifier, calculateInvestedBonus, calculateSkills, isInvested} from './skills';
import {Storage} from '../structures/structures';
import {
    AbilityScores,
    Activity,
    allArmyActivities,
    allLeadershipActivities,
    allRegionActivities, oncePerRoundActivity, trainedActivities,
} from '../actions-and-skills';
import {
    getAllSettlementSceneData,
    getAllSettlementSceneDataAndStructures,
    SettlementSceneData,
} from '../structures/scene';
import {allFeats, allFeatsByName} from './feats';
import {addGroupDialog} from './add-group-dialog';
import {AddBonusFeatDialog} from './add-bonus-feat-dialog';
import {addOngoingEventDialog} from './add-ongoing-event-dialog';
import {rollCultEvent, rollKingdomEvent} from '../kingdom-events';
import {calculateEventXP, calculateHexXP, calculateRpXP} from './xp';
import {setupDialog} from './setup-dialog';
import {featuresByLevel, uniqueFeatures} from './features';

interface KingdomOptions {
    game: Game;
    sheetActor: Actor;
}

type KingdomTabs = 'status' | 'skills' | 'turn' | 'feats' | 'groups' | 'features';

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

    private sheetActor: Actor;

    private readonly game: Game;
    private nav: KingdomTabs = 'status';

    constructor(object: null, options: Partial<FormApplicationOptions> & KingdomOptions) {
        super(object, options);
        this.game = options.game;
        this.sheetActor = options.sheetActor;
    }

    override getData(options?: Partial<FormApplicationOptions>): object {
        const isGM = this.game.user?.isGM ?? false;
        const kingdomData = this.getKingdom();
        const levelData = getLevelData(kingdomData.level);
        const sizeData = getSizeData(kingdomData.size);
        const allSettlementSceneData = getAllSettlementSceneData(this.game);
        const settlementSceneDataAndStructures = getAllSettlementSceneDataAndStructures(this.game);
        const {
            leadershipActivityNumber,
            settlementConsumption,
            storage,
        } = this.getSettlementData(settlementSceneDataAndStructures);
        const totalConsumption = kingdomData.consumption.armies + kingdomData.consumption.now + settlementConsumption;
        const useXpHomebrew = getBooleanSetting(this.game, 'vanceAndKerensharaXP');
        const homebrewSkillIncreases = getBooleanSetting(this.game, 'kingdomSkillIncreaseEveryLevel');
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
            heartland: {
                value: kingdomData.heartland,
                label: capitalize(kingdomData.heartland),
            },
            government: kingdomData.government,
            type: capitalize(sizeData.type),
            controlDC: getControlDC(kingdomData.level, kingdomData.size),
            atWar: kingdomData.atWar,
            unrest: kingdomData.unrest,
            anarchyAt: this.calculateAnarchy(kingdomData.feats, kingdomData.bonusFeats),
            resourceDieSize: sizeData.resourceDieSize,
            resourceDiceNum: levelData.resourceDice + this.insiderTradingResources(kingdomData.feats, kingdomData.bonusFeats),
            resourceDice: kingdomData.resourceDice,
            resourcePoints: kingdomData.resourcePoints,
            consumption: kingdomData.consumption,
            activeSettlement: kingdomData.activeSettlement,
            levels,
            settlementConsumption,
            totalConsumption,
            ruin: this.getRuin(kingdomData.ruin),
            commodities: this.getCommodities(
                kingdomData.commodities.now,
                kingdomData.commodities.next,
                sizeData.commodityCapacity,
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
                alwaysAddLevel: getBooleanSetting(this.game, 'kingdomAlwaysAddLevel'),
            }),
            leaders: this.getLeaders(kingdomData.leaders),
            abilities: this.getAbilities(kingdomData.abilityScores, kingdomData.leaders, kingdomData.level),
            fameTypes: allFameTypes,
            fameLabel: kingdomData.fameType === 'famous' ? 'Fame' : 'Infamy',
            groups: kingdomData.groups,
            ...this.getFeats(kingdomData.feats, kingdomData.bonusFeats, kingdomData.level),
            tradeAgreementsSize: kingdomData.groups.filter(t => t.relations === 'trade-agreement').length,
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
            groupRelationTypes: [
                {label: 'None', value: 'none'},
                {label: 'Diplomatic Relations', value: 'diplomatic-relations'},
                {label: 'Trade Agreement', value: 'trade-agreement'},
            ],
            ongoingEvents: kingdomData.ongoingEvents,
            // TODO: sort by xp and name
            milestones: kingdomData.milestones.map(m => {
                return {...m, display: useXpHomebrew || !m.homebrew};
            }),
            // TODO: filter out companion activities if not in position of leader
            // TODO: consider companions in leadership positions
            leadershipActivities: allLeadershipActivities.map(activity => {
                return {label: this.createActivityLabel(activity, kingdomData.level), value: activity};
            }),
            regionActivities: allRegionActivities.map(activity => {
                return {label: this.createActivityLabel(activity, kingdomData.level), value: activity};
            }),
            armyActivities: allArmyActivities.map(activity => {
                return {label: this.createActivityLabel(activity, kingdomData.level), value: activity};
            }),
            featuresByLevel: Array.from(featuresByLevel.entries())
                .map(([level, features]) => {
                    const featureNames = features.map(f => f.name);
                    if (homebrewSkillIncreases && level % 2 == 0) {
                        featureNames.push('Skill Increase');
                    }
                    return {level, features: featureNames.join(', ')};
                }),
            uniqueFeatures: uniqueFeatures,
            canLevelUp: kingdomData.xp >= kingdomData.xpThreshold && kingdomData.level < 20,
            turnsWithoutEvent: kingdomData.turnsWithoutEvent,
            eventDC: this.calculateEventDC(kingdomData.turnsWithoutEvent),
            civicPlanning: kingdomData.level >= 12,
            useXpHomebrew,
            // TODO: print debug for xp to chat
            // TODO: print debug for end turn to chat
            // TODO: print debug for adjust unrest
            // TODO: print debug for collect resources
            // TODO: print debug for pay consumption
        };
    }

    private createActivityLabel(activity: Activity, kingdomLevel: number): string {
        let label = unslugifyAction(activity);
        if (activity === 'claim-hex') {
            if (kingdomLevel >= 9) {
                label += ' (three times per round)';
            } else if (kingdomLevel >= 4) {
                label += ' (twice per round)';
            } else {
                label += ' (once per round)';
            }
        }
        if (trainedActivities.has(activity) && oncePerRoundActivity.has(activity)) {
            label += ' (once per round, trained)';
        } else if (trainedActivities.has(activity)) {
            label += ' (trained)';
        } else if (oncePerRoundActivity.has(activity)) {
            label += ' (once per round)';
        }
        return label;
    }

    private getActiveTabs(): object {
        return {
            statusTab: this.nav === 'status',
            skillsTab: this.nav === 'skills',
            turnTab: this.nav === 'turn',
            groupsTab: this.nav === 'groups',
            featsTab: this.nav === 'feats',
            featuresTab: this.nav === 'features',
        };
    }

    /* eslint-disable @typescript-eslint/no-explicit-any */
    override async _updateObject(event: Event, formData: any): Promise<void> {
        console.log(formData);
        const milestones = this.getKingdom().milestones;
        const kingdom = expandObject(formData);
        kingdom.groups = unpackFormArray(kingdom.groups);
        kingdom.feats = unpackFormArray(kingdom.feats);
        kingdom.bonusFeats = unpackFormArray(kingdom.bonusFeats);
        kingdom.milestones = unpackFormArray(kingdom.milestones).map((milestone, index) => {
            return {
                ...milestones[index],
                completed: (milestone as {completed: boolean}).completed,
            };
        });
        console.log(kingdom);
        await this.saveKingdom(kingdom);
        this.render();
    }

    private async update(data: Partial<Kingdom>): Promise<void> {
        await this.saveKingdom(data);
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
            ?.addEventListener('click', async () =>
                await this.update({fame: Math.min(3, this.getKingdom().fame + 1)}));
        $html.querySelector('#km-adjust-unrest')
            ?.addEventListener('click', async () => await this.adjustUnrest());
        $html.querySelector('#km-collect-resources')
            ?.addEventListener('click', async () => await this.collectResources());
        $html.querySelector('#km-reduce-unrest')
            ?.addEventListener('click', async () => await this.reduceUnrest());
        $html.querySelector('#km-pay-consumption')
            ?.addEventListener('click', async () => await this.payConsumption());
        $html.querySelector('#km-check-event')
            ?.addEventListener('click', async () => await this.checkForEvent());
        $html.querySelector('#km-roll-event')
            ?.addEventListener('click', async () => await rollKingdomEvent(this.game));
        $html.querySelector('#km-roll-cult-event')
            ?.addEventListener('click', async () => await rollCultEvent(this.game));
        $html.querySelector('#km-add-event')
            ?.addEventListener('click', async () => addOngoingEventDialog((name) => {
                const current = this.getKingdom();
                this.update({
                    ongoingEvents: [...current.ongoingEvents, {name}],
                });
            }));
        $html.querySelectorAll('.km-remove-event')
            ?.forEach(el => {
                el.addEventListener('click', async (ev) => await this.deleteKingdomPropertyAtIndex(ev, 'ongoingEvents'));
            });
        $html.querySelectorAll('.km-event-xp')
            ?.forEach(el => {
                el.addEventListener('click', async (ev) => {
                    const current = this.getKingdom();
                    const target = ev.target as HTMLButtonElement;
                    const modifier = parseInt(target.dataset.modifier ?? '0', 10);
                    await this.update({
                        xp: calculateEventXP(modifier) + current.xp,
                    });
                });
            });
        $html.querySelectorAll('.km-claimed-hexes-xp')
            ?.forEach(el => {
                el.addEventListener('click', async (ev) => {
                    const target = ev.target as HTMLButtonElement;
                    const hexes = parseInt(target.dataset.hexes ?? '0', 10);
                    const current = this.getKingdom();
                    const useHomeBrew = getBooleanSetting(this.game, 'vanceAndKerensharaXP');
                    await this.update({
                        xp: calculateHexXP(hexes, current.size, useHomeBrew) + current.xp,
                    });
                });
            });
        $html.querySelector('#km-rp-to-xp')
            ?.addEventListener('click', async () => {
                const current = this.getKingdom();
                const useHomeBrew = getBooleanSetting(this.game, 'vanceAndKerensharaXP');
                await this.update({
                    xp: calculateRpXP(current.resourcePoints.now, current.level, useHomeBrew) + current.xp,
                });
            });
        $html.querySelector('#km-level-up')
            ?.addEventListener('click', async () => {
                const current = this.getKingdom();
                if (current.xp >= current.xpThreshold) {
                    await this.update({level: current.level + 1, xp: current.xp - current.xpThreshold});
                } else {
                    ui.notifications?.error('Can not level up, not enough XP');
                }
            });
        $html.querySelector('#km-add-group')
            ?.addEventListener('click', async () => {
                addGroupDialog((group) => this.update({
                    groups: [...this.getKingdom().groups, group],
                }));
            });
        $html.querySelectorAll('.km-delete-group')
            ?.forEach(el => {
                el.addEventListener('click', async (ev) => await this.deleteKingdomPropertyAtIndex(ev, 'groups'));
            });
        $html.querySelector('#km-add-bonus-feat')
            ?.addEventListener('click', async () => {
                new AddBonusFeatDialog(null, {
                    feats: allFeats,
                    onOk: (feat) => this.update({
                        bonusFeats: [...this.getKingdom().bonusFeats, feat],
                    }),
                }).render(true);
            });
        $html.querySelectorAll('.km-delete-bonus-feat')
            ?.forEach(el => {
                el.addEventListener('click', async (ev) => await this.deleteKingdomPropertyAtIndex(ev, 'bonusFeats'));
            });
        $html.querySelectorAll('.kingdom-activity')
            ?.forEach(el => {
                el.addEventListener('click', async (el) => {
                    const target = el.target as HTMLButtonElement;
                    const activity = target.dataset.activity;
                    console.warn('run kingdom activity ' + activity, el);
                    // TODO
                });
            });
        $html.querySelector('#km-end-turn')
            ?.addEventListener('click', async () => await this.endTurn());
    }


    private async endTurn(): Promise<void> {
        const current = this.getKingdom();
        const sizeData = getSizeData(current.size);
        const capacity = this.getCapacity(sizeData);
        console.log(capacity);
        await this.update({
            resourceDice: {
                now: current.resourcePoints.next,
                next: 0,
            },
            resourcePoints: {
                now: current.resourcePoints.next,
                next: 0,
            },
            consumption: {
                now: current.consumption.next,
                next: 0,
                armies: current.consumption.armies,
            },
            commodities: {
                now: {
                    food: Math.min(capacity.food, current.commodities.now.food + current.commodities.next.food),
                    luxuries: Math.min(capacity.luxuries, current.commodities.now.luxuries + current.commodities.next.luxuries),
                    stone: Math.min(capacity.stone, current.commodities.now.stone + current.commodities.next.stone),
                    lumber: Math.min(capacity.lumber, current.commodities.now.lumber + current.commodities.next.lumber),
                    ore: Math.min(capacity.ore, current.commodities.now.ore + current.commodities.next.ore),
                },
                next: {
                    food: 0,
                    luxuries: 0,
                    stone: 0,
                    lumber: 0,
                    ore: 0,
                },
            },
        });
    }

    private async adjustUnrest(): Promise<void> {
        const current = this.getKingdom();
        const data = getAllSettlementSceneDataAndStructures(this.game);
        const atWar = current.atWar ? 1 : 0;
        const overcrowdedSettlements = data.filter(s => s.scenedData.overcrowded).length;
        const secondaryTerritories = data.some(s => s.scenedData.secondaryTerritory) ? 1 : 0;
        const newUnrest = atWar + overcrowdedSettlements + secondaryTerritories;
        let unrest = newUnrest + current.unrest;
        if (current.level >= 20 && unrest > 0) {
            unrest = 0;
            await ChatMessage.create({content: 'Ignoring Unrest increase due to "Envy of the World" Kingdom Feature'});
        }
        if (unrest >= 10) {
            const ruinRoll = await (new Roll('1d10').roll());
            await ruinRoll.toMessage({flavor: 'Gaining points to Ruin (distribute as you wish)'});
            const roll = await (new Roll('1d20').roll());
            await roll.toMessage({flavor: 'Check if losing a hex on DC 11'});
            if (roll.total >= 11) {
                await ChatMessage.create({content: 'You lose one hex of your choice'});
            }
        }
        if (unrest >= this.calculateAnarchy(current.feats, current.bonusFeats)) {
            await ChatMessage.create({content: 'Kingdom falls into anarchy, unless you spend all fame/infamy points. Only Quell Unrest leadership activities can be performed and all checks are worsened by a degree'});
        }
        await this.update({
            unrest,
        });
    }

    private async checkForEvent(): Promise<void> {
        const rollMode = getStringSetting(this.game, 'kingdomEventRollMode') as unknown as keyof CONFIG.Dice.RollModes;
        const turnsWithoutEvent = this.getKingdom().turnsWithoutEvent;
        const dc = this.calculateEventDC(turnsWithoutEvent);
        const roll = await (new Roll('1d20').roll());
        await roll.toMessage({flavor: `Checking for Event on DC ${dc}`}, {rollMode});
        if (roll.total >= dc) {
            await ChatMessage.create({
                type: CONST.CHAT_MESSAGE_TYPES.ROLL,
                content: 'An event occurs, roll a Kingdom Event!',
                rollMode,
            });
            await this.update({turnsWithoutEvent: 0});
        } else {
            await this.update({turnsWithoutEvent: turnsWithoutEvent + 1});
        }
    }

    private async deleteKingdomPropertyAtIndex(ev: Event, property: keyof Kingdom): Promise<void> {
        const target = ev.currentTarget as HTMLButtonElement;
        const deleteIndex = target.dataset.deleteIndex;
        if (deleteIndex) {
            const deleteAt = parseInt(deleteIndex, 10);
            const values = [...this.getKingdom()[property] as unknown[]];
            values.splice(deleteAt, 1);
            await this.update({
                [property]: values,
            });
        }
    }

    /**
     * Hardcode for now
     */
    private insiderTradingResources(feats: Feat[], bonusFeat: BonusFeat[]): number {
        const hasInsiderTrading = feats.some(f => f.id === 'Insider Trading') ||
            bonusFeat.some(f => f.id === 'Insider Trading');
        return hasInsiderTrading ? 1 : 0;
    }

    private async collectResources(): Promise<void> {
        const current = this.getKingdom();
        const levelData = getLevelData(current.size);
        const featDice = this.insiderTradingResources(current.feats, current.bonusFeats);
        const sizeData = getSizeData(current.size);
        const capacity = this.getCapacity(sizeData);
        const dice = levelData.resourceDice + current.resourceDice.now + featDice;
        const rolledPoints = await this.rollResourceDice(sizeData.resourceDieSize, dice);
        await this.update({
            resourcePoints: {
                now: current.resourcePoints.now + rolledPoints,
                next: current.resourcePoints.next,
            },
            resourceDice: {
                now: 0,
                next: current.resourceDice.next,
            },
            commodities: {
                now: {
                    ...this.calculateCommoditiesThisTurn(capacity, current),
                    food: current.commodities.now.food,
                },
                next: current.commodities.next,
            },
        });
    }

    private getCapacity(sizeData: KingdomSizeData): Commodities {
        const settlementSceneDataAndStructures = getAllSettlementSceneDataAndStructures(this.game);
        const {storage} = this.getSettlementData(settlementSceneDataAndStructures);
        return this.calculateStorageCapacity(sizeData.commodityCapacity, storage);
    }

    private calculateCommoditiesThisTurn(
        capacity: Commodities,
        kingdom: Kingdom,
    ): Omit<Commodities, 'food'> {
        const sites = kingdom.workSites;
        return {
            ore: Math.min(capacity.ore, sites.mines.quantity + sites.mines.resources),
            lumber: Math.min(capacity.lumber, sites.mines.quantity + sites.mines.resources),
            luxuries: Math.min(capacity.luxuries, sites.luxurySources.quantity + sites.luxurySources.resources),
            stone: Math.min(capacity.stone, sites.lumberCamps.quantity + sites.lumberCamps.resources),
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
                unrest: Math.max(0, this.getKingdom().unrest - 1),
            });
        }
    }

    private calculateEventDC(turnsWithoutEvent: number): number {
        return Math.max(1, 16 - (turnsWithoutEvent * 5));
    }

    private calculateAnarchy(feats: Feat[], bonusFeats: BonusFeat[]): number {
        const hasEndureAnarchy = feats.some(f => f.id === 'Endure Anarchy') ||
            bonusFeats.some(f => f.id === 'Endure Anarchy');
        return hasEndureAnarchy ? 24 : 20;
    }

    private async payConsumption(): Promise<void> {
        const current = this.getKingdom();
        const settlementSceneDataAndStructures = getAllSettlementSceneDataAndStructures(this.game);
        const {settlementConsumption} = this.getSettlementData(settlementSceneDataAndStructures);
        const totalConsumption = current.consumption.armies + current.consumption.now + settlementConsumption;
        const currentFood = current.commodities.now.food;
        if (totalConsumption > currentFood) {
            const missingFood = totalConsumption - currentFood;
            const pay = missingFood * 5;
            await ChatMessage.create({content: `Missing ${missingFood} food commodities. Either pay ${pay} RP or gain [[/r 1d4]] Unrest`});
        }
        if (totalConsumption > 0) {
            await this.update({
                commodities: {
                    now: {
                        ...current.commodities.now,
                        food: Math.max(0, currentFood - totalConsumption),
                    },
                    next: current.commodities.next,
                },
            });
        }
    }

    private async saveKingdom(kingdom: Partial<Kingdom>): Promise<void> {
        await this.sheetActor.setFlag('pf2e-kingmaker-tools', 'kingdom-sheet', kingdom);
    }

    private getKingdom(): Kingdom {
        return this.sheetActor.getFlag('pf2e-kingmaker-tools', 'kingdom-sheet') as Kingdom;
    }
}

export async function showKingdom(game: Game): Promise<void> {
    const sheetActor = game?.actors?.find(a => a.name === 'Kingdom Sheet');
    if (sheetActor) {
        new KingdomApp(null, {game, sheetActor}).render(true);
    } else {
        setupDialog(game, async () => {
            const sheetActor = game?.actors?.find(a => a.name === 'Kingdom Sheet');
            await sheetActor?.setFlag('pf2e-kingmaker-tools', 'kingdom-sheet', getDefaultKingdomData());
            await showKingdom(game);
        });
    }
}
