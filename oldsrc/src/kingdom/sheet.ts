import {
    allFameTypes,
    allHeartlands,
    BonusFeat,
    Commodities,
    FameType,
    Feat,
    getControlDC,
    getDefaultKingdomData,
    getSizeData,
    Kingdom,
    Leaders,
    LeaderValues,
    Ruin,
    Settlement,
    WorkSites,
} from './data/kingdom';
import {
    capitalize,
    clamped,
    deCamelCase,
    distinctBy,
    groupBy,
    isNonNullable,
    postChatMessage,
    range,
    toLabelAndValue,
    unpackFormArray,
    unslugify,
} from '../utils';
import {
    getActiveSettlementStructureResult,
    getAllMergedSettlements,
    getCurrentScene,
    getScene,
    getSettlement,
    getSettlementInfo,
    getStolenLandsData,
    getStructureResult,
    getStructuresByName,
    getStructureStackMode,
    SettlementAndScene,
} from './scene';
import {getAllFeats, getAllSelectedFeats, KingdomFeat} from './data/feats';
import {AddBonusFeatDialog} from './dialogs/add-bonus-feat-dialog';
import {setupDialog} from './dialogs/setup-dialog';
import {getAllFeatures} from './data/features';
import {createActivityLabel, getPerformableActivities, groupKingdomActivities,} from './data/activities';
import {Ability, AbilityScores, calculateAbilityModifier} from './data/abilities';
import {calculateInvestedBonus, isInvested, Leader} from './data/leaders';
import {showHelpDialog} from './dialogs/show-help-dialog';
import {showSettlement} from './dialogs/settlement';
import {getKingdom, saveKingdom} from './storage';
import {gainFame, getCapacity, getConsumption} from './kingdom-utils';
import {openJournal} from '../foundry-utils';
import {showStructureBrowser} from './dialogs/structure-browser';
import {gainUnrest, getKingdomActivitiesById, loseRP} from './data/activityData';
import {manageKingdomActivitiesDialog} from './dialogs/activities-dialog';
import {calculateResourceDicePerTurn} from "./structures";
import {Skill} from "./data/skills";

interface KingdomOptions {
    game: Game;
    sheetActor: Actor;
}

type KingdomTab = 'status' | 'skills' | 'turn' | 'feats' | 'groups' | 'notes' | 'settlements' | 'effects';

const levels = [...Array.from(Array(20).keys()).map(k => k + 1)];

interface Effect {
    name: string;
    effect: string;
    turns: string;
    consumable: boolean;
}

function createEffects(modifiers: any[]): Effect[] {
    return modifiers.map(modifier => {
        return {
            name: modifier.name,
            effect: modifier.buttonLabel ?? modifier.name,
            turns: modifier.turns === undefined ? 'indefinite' : `${modifier.turns}`,
            consumable: modifier.isConsumedAfterRoll === true,
        };
    });
}

export interface ModifierTotal {
    bonus: number;
    penalty: number;
}

export interface ModifierTotals {
    item: ModifierTotal;
    circumstance: ModifierTotal;
    status: ModifierTotal;
    ability: ModifierTotal;
    proficiency: ModifierTotal;
    untyped: ModifierTotal;
    leadership: ModifierTotal;
    vacancyPenalty: number;
    value: number;
}

export interface SkillStats {
    skill: Skill;
    skillLabel: string;
    ability: Ability;
    abilityLabel: string;
    rank: number;
    total: ModifierTotals;
}

class KingdomApp extends FormApplication<FormApplicationOptions & KingdomOptions, object, Kingdom> {
    private sheetActor: Actor;
    private readonly game: Game;
    private nav: KingdomTab = 'turn';

    constructor(object: Kingdom, options: Partial<FormApplicationOptions> & KingdomOptions) {
        super(object, options);
        this.game = options.game;
        this.sheetActor = options.sheetActor;
        this.sheetActor.apps[this.appId] = this;
    }

    static override get defaultOptions(): FormApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'kingdom-app';
        options.title = 'Kingdom';
        options.template = 'modules/pf2e-kingmaker-tools/templates/kingdom/sheet.hbs';
        options.submitOnChange = true;
        options.closeOnSubmit = false;
        options.classes = ['kingmaker-tools-app', 'kingdom-app'];
        options.width = 850;
        options.height = 'auto';
        options.scrollY = ['.km-content', '.km-sidebar'];
        return options;
    }

    override async getData(): Promise<object> {
        const isGM = this.game.user?.isGM ?? false;
        const kingdomData = this.getKingdom();
        this.object = kingdomData;
        const automateResourceMode = kingdomData.settings.automateResources;
        const autoCalculateArmyConsumption = kingdomData.settings.autoCalculateArmyConsumption
        const {size: kingdomSize, workSites} = getStolenLandsData(this.game, automateResourceMode, kingdomData);
        const sizeData = getSizeData(kingdomSize);
        const autoCalculateSettlementLevel = kingdomData.settings.autoCalculateSettlementLevel;
        const {
            leadershipActivityNumber,
            settlementConsumption,
        } = getAllMergedSettlements(this.game, kingdomData);
        const {current: totalConsumption, surplus: farmSurplus} = getConsumption(this.game, kingdomData);
        const useXpHomebrew = kingdomData.settings.vanceAndKerensharaXP;
        const activeSettlementStructureResult = getActiveSettlementStructureResult(this.game, kingdomData);
        const activeSettlement = getSettlement(this.game, kingdomData, kingdomData.activeSettlement);
        const hideActivities = kingdomData.activityBlacklist
            .map(activity => {
                return {[activity]: true};
            })
            .reduce((a, b) => Object.assign(a, b), {});
        const ignoreSkillRequirements = kingdomData.settings.kingdomIgnoreSkillRequirements;
        const activities = getKingdomActivitiesById(this.game, kingdomData.homebrewActivities);
        const enabledActivities = getPerformableActivities(
            kingdomData.skillRanks,
            activeSettlementStructureResult?.active?.allowCapitalInvestment === true
            || (activeSettlement?.settlement.type === 'capital' && kingdomData.settings.capitalInvestmentInCapital),
            ignoreSkillRequirements,
            activities,
        );
        const groupedActivities = groupKingdomActivities(activities);
        const currentSceneId = getCurrentScene(this.game)?.id;
        const canAddSettlement = kingdomData.settlements.find(settlement => settlement.sceneId === currentSceneId) === undefined;
        const canAddRealm = isNonNullable(currentSceneId) && currentSceneId !== kingdomData.realmSceneId;
        const structureStackMode = getStructureStackMode(kingdomData);
        const automateResources = automateResourceMode !== 'manual';
        const showAddRealmButton = isGM && automateResourceMode === 'tileBased';
        const showRealmData = automateResourceMode === 'kingmaker'
            || automateResourceMode === 'manual'
            || (isNonNullable(kingdomData.realmSceneId) && this.game.scenes?.find(s => s.id === kingdomData.realmSceneId) !== undefined);
        const featuresByLevel = groupBy(getAllFeatures(this.game, kingdomData), f => f.level);
        return {
            notes: {
                gm: await TextEditor.enrichHTML(kingdomData.notes.gm),
                public: await TextEditor.enrichHTML(kingdomData.notes.public),
            },
            hideActivities,
            isGM,
            isUser: !isGM,
            enabledActivities: enabledActivities,
            leadershipActivityNumber: leadershipActivityNumber,
            name: kingdomData.name,
            size: kingdomSize,
            xp: kingdomData.xp,
            xpThreshold: kingdomData.xpThreshold,
            level: kingdomData.level,
            fame: kingdomData.fame,
            charter: kingdomData.charter,
            heartland: kingdomData.heartland,
            heartlandLabel: unslugify(kingdomData.heartland),
            government: kingdomData.government,
            type: capitalize(sizeData.type),
            controlDC: getControlDC(kingdomData.level, kingdomSize, kingdomData.leaders.ruler.vacant),
            atWar: kingdomData.atWar,
            unrest: kingdomData.unrest,
            unrestPenalty: this.game.pf2eKingmakerTools.migration.calculateUnrestPenalty(kingdomData.unrest),
            anarchyAt: this.calculateAnarchy(kingdomData.feats, kingdomData.bonusFeats),
            resourceDieSize: sizeData.resourceDieSize,
            resourceDiceNum: calculateResourceDicePerTurn(this.game, kingdomData),
            resourceDice: kingdomData.resourceDice,
            resourcePoints: kingdomData.resourcePoints,
            consumption: kingdomData.consumption,
            activeSettlement: kingdomData.activeSettlement,
            supernaturalSolutions: kingdomData.supernaturalSolutions,
            creativeSolutions: kingdomData.creativeSolutions,
            autoCalculateArmyConsumption,
            levels: toLabelAndValue(levels),
            settlementConsumption,
            totalConsumption,
            farmSurplus,
            ruin: this.getRuin(kingdomData.ruin),
            commodities: this.getCommodities(kingdomData),
            workSites: this.getWorkSites(workSites),
            farmlands: workSites.farmlands.quantity,
            food: workSites.farmlands.resources,
            ...this.getActiveTabs(),
            skills: await this.game.pf2eKingmakerTools.migration.calculateSkillModifiers(
                this.game,
                kingdomData,
            ),
            leaders: await this.getLeaders(kingdomData.leaders),
            abilities: this.getAbilities(kingdomData.abilityScores, kingdomData.leaders, kingdomData.level),
            fameTypes: allFameTypes,
            fameLabel: this.getFameLabel(kingdomData.fame.type),
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
            heartlands: allHeartlands.map(heartland => {
                return {label: unslugify(heartland), value: heartland};
            }),
            fameValues: toLabelAndValue(range(0, kingdomData.settings.maximumFamePoints + 1)),
            aspirations: [{value: 'famous', label: 'Fame'}, {value: 'infamous', label: 'Infamy'}],
            settlements: kingdomData.settlements
                .map(settlement => getSettlement(this.game, kingdomData, settlement.sceneId))
                .filter(settlement => settlement !== undefined)
                .map(settlement => {
                    const s = settlement as SettlementAndScene;
                    const waterBorders = s.settlement.waterBorders ?? 0;
                    const structureResult = getStructureResult(structureStackMode, autoCalculateSettlementLevel, activities, getStructuresByName(this.game), s);
                    return {
                        ...s.settlement,
                        ...getSettlementInfo(s, autoCalculateSettlementLevel),
                        waterBorders,
                        overcrowded: this.isOvercrowded(kingdomData, s),
                        residentialLots: structureResult.residentialLots,
                        lacksBridge: waterBorders >= 4 && !structureResult.hasBridge,
                        isCapital: settlement?.settlement.type === 'capital',
                        name: s.scene.name ?? undefined,
                    };
                }),
            groupRelationTypes: [
                {label: 'None', value: 'none'},
                {label: 'Diplomatic Relations', value: 'diplomatic-relations'},
                {label: 'Trade Agreement', value: 'trade-agreement'},
            ],
            ongoingEvents: await Promise.all(kingdomData.ongoingEvents
                .map(event => TextEditor.enrichHTML(event.name))),
            milestones: kingdomData.milestones.map(m => {
                return {...m, display: (useXpHomebrew || !m.homebrew) && (isGM || !m.name.startsWith('Cult Event'))};
            }),
            leadershipActivities: Array.from(groupedActivities['leadership'])
                .map(activity => {
                    return {
                        label: createActivityLabel(this.game, groupedActivities, activity, kingdomData),
                        value: activity
                    };
                })
                .sort((a, b) => a.label.localeCompare(b.label)),
            regionActivities: Array.from(groupedActivities['region'])
                .map(activity => {
                    return {
                        label: createActivityLabel(this.game, groupedActivities, activity, kingdomData),
                        value: activity
                    };
                }),
            armyActivities: Array.from(groupedActivities['army'])
                .map(activity => {
                    return {
                        label: createActivityLabel(this.game, groupedActivities, activity, kingdomData),
                        value: activity
                    };
                }),
            featuresByLevel: Array.from(featuresByLevel.entries())
                .sort(([a], [b]) => a - b)
                .map(([level, features]) => {
                    const featureNames = features.map(f => f.name);
                    return {level, features: featureNames.join(', ')};
                }),
            uniqueFeatures: distinctBy(getAllFeatures(this.game, kingdomData), (a) => a.name),
            canLevelUp: kingdomData.xp >= kingdomData.xpThreshold && kingdomData.level < 20,
            turnsWithoutEvent: kingdomData.turnsWithoutEvent,
            eventDC: this.calculateEventDC(kingdomData.turnsWithoutEvent),
            turnsWithoutCultEvent: kingdomData.turnsWithoutCultEvent,
            cultEventDC: this.calculateCultEventDC(kingdomData.turnsWithoutCultEvent),
            civicPlanning: kingdomData.level >= 12,
            useXpHomebrew,
            canAddSettlement,
            effects: createEffects(kingdomData.modifiers),
            cultOfTheBloomEvents: kingdomData.settings.cultOfTheBloomEvents && isGM,
            automateResources,
            canAddRealm,
            showRealmData,
            showAddRealmButton,
            enableLeadershipModifiers: kingdomData.settings.enableLeadershipModifiers,
            leaderTypes: allLeaderTypes.map(t => {
                return {label: deCamelCase(t), value: t};
            }),
        };
    }

    /* eslint-disable @typescript-eslint/no-explicit-any */
    override async _updateObject(event: Event, formData: any): Promise<void> {
        console.log(formData);
        const milestones = this.getKingdom().milestones;
        const kingdom = foundry.utils.expandObject(formData);
        kingdom.groups = unpackFormArray(kingdom.groups);
        kingdom.feats = unpackFormArray(kingdom.feats);
        kingdom.bonusFeats = unpackFormArray(kingdom.bonusFeats);
        kingdom.milestones = unpackFormArray(kingdom.milestones).map((milestone, index) => {
            return {
                ...milestones[index],
                completed: (milestone as { completed: boolean }).completed,
            };
        });
        await this.saveKingdom(kingdom);
    }

    public sceneChange(): void {
        this.render();
    }

    private updateActor(actor: Actor): void {
        const leaderUuids = Object.values(this.getKingdom().leaders).map(a => a.uuid);
        if (leaderUuids.includes(actor.uuid)) {
            this.render();
        }
    }

    private updateItem(item: Item): void {
        const actor = item.actor;
        if (item.type === 'lore' && actor) {
            this.updateActor(actor)
        }
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        Hooks.on('closeKingmakerHexEdit', this.sceneChange.bind(this));
        Hooks.on('deleteScene', this.sceneChange.bind(this));
        Hooks.on('canvasReady', this.sceneChange.bind(this));
        Hooks.on('createToken', this.sceneChange.bind(this));
        Hooks.on('deleteToken', this.sceneChange.bind(this));
        Hooks.on('sightRefresh', this.sceneChange.bind(this)); // end of drag movement
        Hooks.on('applyTokenStatusEffect', this.sceneChange.bind(this));
        Hooks.on('createTile', this.sceneChange.bind(this));
        Hooks.on('updateTile', this.sceneChange.bind(this));
        Hooks.on('deleteTile', this.sceneChange.bind(this));
        Hooks.on('createDrawing', this.sceneChange.bind(this));
        Hooks.on('updateDrawing', this.sceneChange.bind(this));
        Hooks.on('deleteDrawing', this.sceneChange.bind(this));
        Hooks.on('updateActor', this.updateActor.bind(this));
        Hooks.on('updateItem', this.updateItem.bind(this));
        const $html = html[0];
        $html.querySelectorAll('.km-leader-details')
            .forEach(el => {
                const elem = el as HTMLElement;
                elem.addEventListener('drop', async (ev) => {
                    const leader = elem.dataset.leader as Leader;
                    const data = ev.dataTransfer?.getData("text/plain");
                    if (data) {
                        const json = JSON.parse(data);
                        if ('type' in json && json['type'] === 'Actor') {
                            const uuid = json['uuid'];
                            const actor = (await fromUuid(uuid)) as Actor | null;
                            if (actor && (actor.type === 'character' || actor.type === 'npc')) {
                                const kingdom = this.getKingdom();
                                kingdom.leaders[leader].uuid = uuid;
                                await this.saveKingdom(kingdom)
                            } else {
                                ui?.notifications?.error('Can only set Characters or NPCs as leaders');
                            }
                        }
                    }
                });
            });
        $html.querySelectorAll('.km-leader-details [data-action=open-actor]').forEach(el => {
            el.addEventListener('click', async (ev) => {
                ev.preventDefault();
                ev.stopPropagation();
                const elem = ev.currentTarget as HTMLElement;
                const closest = elem.closest('.km-leader-details') as HTMLElement;
                const uuid = closest.dataset.uuid as string;
                const actor = (await fromUuid(uuid)) as Actor | null;
                if (actor) {
                    actor.sheet?.render(true);
                }
            });
        });
        $html.querySelectorAll('.km-leader-details [data-action=remove-actor]').forEach(el => {
            el.addEventListener('click', async (ev) => {
                const elem = ev.currentTarget as HTMLElement;
                const closest = elem.closest('.km-leader-details') as HTMLElement;
                const leader = closest.dataset.leader as Leader;
                const kingdom = this.getKingdom();
                kingdom.leaders[leader].uuid = null;
                await this.saveKingdom(kingdom)
            });
        });
        $html.querySelectorAll('.km-nav a')?.forEach(el => {
            el.addEventListener('click', (event) => {
                const tab = event.currentTarget as HTMLAnchorElement;
                this.nav = tab.dataset.tab as KingdomTab;
                this.render();
            });
        });
        $html.querySelectorAll('.km-reduce-ruin')
            .forEach(el => el.addEventListener('click', async (ev) => await this.reduceRuin(ev)));
        $html.querySelector('#km-kingdom-size-help')
            ?.addEventListener('click', async (ev) => {
                ev.stopPropagation();
                ev.preventDefault();
                this.game.pf2eKingmakerTools.migration.kingdomSizeHelp();
            });
        $html.querySelector('#km-settlement-size-help')
            ?.addEventListener('click', async (ev) => {
                ev.stopPropagation();
                ev.preventDefault();
                this.game.pf2eKingmakerTools.migration.settlementSizeHelp();
            });
        $html.querySelector('#km-gain-fame')
            ?.addEventListener('click', async () => await this.upkeepGainFame());
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
        $html.querySelector('#km-check-cult-event')
            ?.addEventListener('click', async () => await this.checkForCultEvent());
        $html.querySelector('#km-roll-event')
            ?.addEventListener('click', async () => this.game.pf2eKingmakerTools.macros.kingdomEventsMacro());
        $html.querySelector('#km-roll-cult-event')
            ?.addEventListener('click', async () => this.game.pf2eKingmakerTools.macros.cultEventsMacro());
        $html.querySelector('#claimed-refuge')
            ?.addEventListener('click', async () => await this.claimedHexFeature('refuge'));
        $html.querySelector('#km-open-structure-browser')
            ?.addEventListener('click', async () => await this.showStructureBrowser());
        $html.querySelector('.kingdom-activity[data-activity=train-army]')
            ?.addEventListener('click', async () => await this.showTacticsBrowser());
        $html.querySelector('.kingdom-activity[data-activity=recruit-army]')
            ?.addEventListener('click', async () => await this.showArmyBrowser());
        $html.querySelectorAll('.km-view-settlement-scene')
            ?.forEach(el => {
                el.addEventListener('click', async (ev: Event): Promise<void> => {
                    await this.viewSettlementScene((ev as MouseEvent));
                });
            });
        $html.querySelector('#claimed-landmark')
            ?.addEventListener('click', async () => await this.claimedHexFeature('landmark'));
        $html.querySelector('#km-add-event')
            ?.addEventListener('click', async () => this.game.pf2eKingmakerTools.migration.addOngoingEventDialog(async (event) => {
                const current = this.getKingdom();
                await this.saveKingdom({
                    ongoingEvents: [...current.ongoingEvents, {name: event}],
                });
            }));
        $html.querySelectorAll('.km-remove-event')
            ?.forEach(el => {
                el.addEventListener('click', async (ev) => await this.deleteKingdomPropertyAtIndex(ev, 'ongoingEvents'));
            });
        $html.querySelectorAll('.km-event-xp')
            ?.forEach(el => {
                el.addEventListener('click', async (ev) => {
                    const target = ev.currentTarget as HTMLButtonElement;
                    const modifier = parseInt(target.dataset.modifier ?? '0', 10);
                    await this.increaseXP(this.game.pf2eKingmakerTools.migration.calculateEventXP(modifier));
                });
            });
        $html.querySelectorAll('.km-claimed-hexes-xp')
            ?.forEach(el => {
                el.addEventListener('click', async (ev) => {
                    const target = ev.currentTarget as HTMLButtonElement;
                    const hexes = parseInt(target.dataset.hexes ?? '0', 10);
                    const current = this.getKingdom();
                    const automateResourceMode = current.settings.automateResources;
                    const {size: kingdomSize} = getStolenLandsData(this.game, automateResourceMode, current);
                    const useHomeBrew = current.settings.vanceAndKerensharaXP;
                    await this.increaseXP(this.game.pf2eKingmakerTools.migration.calculateHexXP(
                        hexes,
                        current.settings.xpPerClaimedHex,
                        kingdomSize,
                        useHomeBrew,
                    ));
                });
            });
        $html.querySelectorAll('.km-gain-xp')
            ?.forEach(el => {
                el.addEventListener('click', async (ev) => {
                    const target = ev.currentTarget as HTMLButtonElement;
                    const xp = parseInt(target.dataset.xp ?? '0', 10);
                    if (xp) {
                        await this.increaseXP(xp);
                    }
                });
            });
        $html.querySelectorAll('.km-gain-structure-xp')
            ?.forEach(el => {
                el.addEventListener('click', async (ev) => {
                    this.game.pf2eKingmakerTools.migration.structureXpDialog(async (xp) => {
                        await this.increaseXP(xp);
                    })
                });
            });
        $html.querySelector('#km-rp-to-xp')
            ?.addEventListener('click', async () => {
                const current = this.getKingdom();
                const useHomeBrew = current.settings.vanceAndKerensharaXP;
                await this.increaseXP(this.game.pf2eKingmakerTools.migration.calculateRpXP(
                    current.resourcePoints.now,
                    current.level,
                    current.settings.rpToXpConversionRate,
                    current.settings.rpToXpConversionLimit,
                    useHomeBrew,
                ));
            });
        $html.querySelector('#solutions-to-xp')
            ?.addEventListener('click', async () => {
                const current = this.getKingdom();
                await this.increaseXP((current.supernaturalSolutions + current.creativeSolutions) * 10);
            });
        $html.querySelector('#km-level-up')
            ?.addEventListener('click', async () => {
                const current = this.getKingdom();
                if (current.xp >= current.xpThreshold) {
                    await this.saveKingdom({level: current.level + 1, xp: current.xp - current.xpThreshold});
                } else {
                    ui.notifications?.error('Can not level up, not enough XP');
                }
            });
        $html.querySelector('#km-add-group')
            ?.addEventListener('click', async () => {
                await this.saveKingdom({
                    groups: [...this.getKingdom().groups, {
                        name: "New Group",
                        negotiationDC: 0,
                        atWar: false,
                        relations: "none"
                    }],
                });
            });
        $html.querySelectorAll('.km-delete-group')
            ?.forEach(el => {
                el.addEventListener('click', async (ev) => await this.deleteKingdomPropertyAtIndex(ev, 'groups'));
            });
        $html.querySelector('#km-add-bonus-feat')
            ?.addEventListener('click', async () => {
                new AddBonusFeatDialog(null, {
                    feats: getAllFeats(this.game, this.getKingdom()),
                    onOk: (feat): Promise<void> => this.saveKingdom({
                        bonusFeats: [...this.getKingdom().bonusFeats, feat],
                    }),
                }).render(true);
            });
        $html.querySelector('#manage-kingdom-activities')
            ?.addEventListener('click', async () => {
                manageKingdomActivitiesDialog(this.game, this.sheetActor);
            });
        $html.querySelectorAll('.km-delete-bonus-feat')
            ?.forEach(el => {
                el.addEventListener('click', async (ev) => await this.deleteKingdomPropertyAtIndex(ev, 'bonusFeats'));
            });
        $html.querySelectorAll('.kingdom-activity:not([data-activity=train-army]):not([data-activity=recruit-army])')
            ?.forEach(el => {
                el.addEventListener('click', async (el) => {
                    const target = el.currentTarget as HTMLButtonElement;
                    const activity = target.dataset.activity!;
                    const kingdom = this.getKingdom();
                    const activityData = getKingdomActivitiesById(this.game, kingdom.homebrewActivities);
                    if (activityData[activity].dc === 'none') {
                        await showHelpDialog(this.game, this.sheetActor, activity);
                    } else {
                        this.game.pf2eKingmakerTools.migration.checkDialog(
                            this.game,
                            kingdom,
                            this.sheetActor,
                            getKingdomActivitiesById(this.game, kingdom.homebrewActivities)[activity],
                            undefined,
                            undefined,
                            undefined,
                        )
                    }
                });
            });
        $html.querySelectorAll('.kingdom-skill')
            ?.forEach(el => {
                el.addEventListener('click', async (el) => {
                    const target = el.currentTarget as HTMLButtonElement;
                    const skill = target.dataset.skill;
                    const kingdom = this.getKingdom();
                    this.game.pf2eKingmakerTools.migration.checkDialog(
                        this.game,
                        kingdom,
                        this.sheetActor,
                        undefined,
                        undefined,
                        skill,
                        undefined,
                    )
                });
            });
        $html.querySelector('#km-end-turn')
            ?.addEventListener('click', async () => await this.endTurn());
        $html.querySelectorAll('.show-help')
            ?.forEach(el => {
                el.addEventListener('click', async (el) => {
                    const target = el.currentTarget as HTMLButtonElement;
                    const help = target.dataset.help!;
                    if (help) {
                        await showHelpDialog(this.game, this.sheetActor, help);
                    }
                });
            });
        $html.querySelector('#make-current-scene-realm')
            ?.addEventListener('click', async () => {
                const scene = getCurrentScene(this.game);
                const realmSceneId = scene?.id;
                if (realmSceneId) {
                    await this.saveKingdom({realmSceneId});
                }
            });
        $html.querySelector('#make-current-scene-settlement')
            ?.addEventListener('click', async () => {
                const scene = getCurrentScene(this.game);
                const id = scene?.id;
                if (id) {
                    const newSettlement: Settlement = {
                        sceneId: id,
                        level: 0,
                        type: 'settlement',
                        lots: 0,
                        secondaryTerritory: false,
                        waterBorders: 0,
                    };
                    const current = this.getKingdom();
                    await this.saveKingdom({
                        settlements: [...current.settlements, newSettlement],
                        activeSettlement: current.settlements.length === 0 ? id : current.activeSettlement,
                    });
                }
            });
        $html.querySelectorAll('.km-delete-settlement')
            ?.forEach(el => {
                el.addEventListener('click', async (ev) => await this.deleteKingdomPropertyAtIndex(ev, 'settlements'));
            });
        $html.querySelectorAll('.inspect-settlement')
            ?.forEach(el => {
                el.addEventListener('click', async (el) => {
                    const current = this.getKingdom();
                    const target = el.currentTarget as HTMLButtonElement;
                    const id = target.dataset.id!;
                    await showSettlement(this.game, id, current);
                });
            });
        $html.querySelector('#km-add-effect')
            ?.addEventListener('click', async () => {
                this.game.pf2eKingmakerTools.migration.addModifier()
            });
        $html.querySelectorAll('.km-delete-effect')
            ?.forEach(el => {
                el.addEventListener('click', async (ev) => await this.deleteKingdomPropertyAtIndex(ev, 'modifiers'));
            });
        $html.querySelector('.kingdom-settings')?.addEventListener('click', () => this.game.pf2eKingmakerTools.migration.kingdomSettings(
            this.getKingdom().settings,
            async (settings) => {
                const kingdom = this.getKingdom();
                await saveKingdom(this.sheetActor, {
                    fame: {
                        ...kingdom.fame,
                        now: clamped(kingdom.fame.now, 0, settings.maximumFamePoints),
                        next: clamped(kingdom.fame.next, 0, settings.maximumFamePoints),
                    },
                    settings,
                })
            }
        ));
        $html.querySelectorAll('.edit-settlement')
            ?.forEach(el => {
                el.addEventListener('click', async (ev) => {
                    const target = ev?.currentTarget as HTMLButtonElement | null;
                    const index = parseInt(target?.dataset?.index as string, 10);
                    const current = this.getKingdom();
                    const data = current.settlements[index];
                    const name = getScene(this.game, data.sceneId)?.name as string;
                    const autoLevel = current.settings.autoCalculateSettlementLevel;
                    this.game.pf2eKingmakerTools.migration.editSettlementDialog(
                        autoLevel,
                        name,
                        data,
                        (savedData) => {
                            current.settlements[index] = savedData;
                            this.saveKingdom({
                                settlements: current.settlements,
                            });
                        });
                });
            });
    }

    override close(options?: FormApplication.CloseOptions): Promise<void> {
        Hooks.off('closeKingmakerHexEdit', this.sceneChange);
        Hooks.off('deleteScene', this.sceneChange);
        Hooks.off('canvasReady', this.sceneChange);
        Hooks.off('createToken', this.sceneChange);
        Hooks.off('sightRefresh', this.sceneChange); // end of drag movement
        Hooks.off('deleteToken', this.sceneChange);
        Hooks.off('applyTokenStatusEffect', this.sceneChange);
        Hooks.off('createTile', this.sceneChange);
        Hooks.off('updateTile', this.sceneChange);
        Hooks.off('deleteTile', this.sceneChange);
        Hooks.off('createDrawing', this.sceneChange);
        Hooks.off('updateDrawing', this.sceneChange);
        Hooks.off('deleteDrawing', this.sceneChange);
        Hooks.off('updateActor', this.updateActor);
        Hooks.off('updateItem', this.updateItem);
        return super.close(options);
    }

    protected _getHeaderButtons(): Application.HeaderButton[] {
        const buttons = super._getHeaderButtons();
        buttons.unshift({
            label: 'Help',
            class: 'pf2e-kingmaker-tools-hb1',
            icon: 'fas fa-question',
            onclick: () => openJournal('Compendium.pf2e-kingmaker-tools.kingmaker-tools-journals.JournalEntry.iAQCUYEAq4Dy8uCY.JournalEntryPage.ty6BS5eSI7ScfVBk'),
        });
        if (this.game.user?.isGM ?? false) {
            buttons.unshift({
                label: 'Show Players',
                class: 'something-made-up',
                icon: 'fas fa-eye',
                onclick: () => this.game.socket!.emit('module.pf2e-kingmaker-tools', {
                    action: 'openKingdomSheet',
                }),
            });
        }
        return buttons;
    }

    private getFameLabel(fameType: FameType): string {
        return fameType === 'famous' ? 'Fame' : 'Infamy';
    }

    private getActiveTabs(): Record<string, boolean> {
        return {
            statusTab: this.nav === 'status',
            skillsTab: this.nav === 'skills',
            turnTab: this.nav === 'turn',
            groupsTab: this.nav === 'groups',
            featsTab: this.nav === 'feats',
            notesTab: this.nav === 'notes',
            settlementsTab: this.nav === 'settlements',
            effectsTab: this.nav === 'effects',
        };
    }

    private async upkeepGainFame(): Promise<void> {
        await ChatMessage.create({content: 'Gaining 1 Fame'});
        await this.saveKingdom(gainFame(this.getKingdom(), 1));
    }

    private async reduceRuin(event: Event): Promise<void> {
        const button = event.currentTarget as HTMLButtonElement;
        const ruin = button.dataset.ruin as keyof Ruin;
        const roll = await new Roll('1d20').roll();
        await roll.toMessage({flavor: `Reducing ${capitalize(ruin)} on a 16 or higher`});
        if (roll.total >= 16) {
            const current = this.getKingdom();
            await this.saveKingdom({
                ruin: {
                    ...current.ruin,
                    [ruin]: {
                        ...current.ruin[ruin],
                        penalty: Math.max(0, current.ruin[ruin].penalty - 1),
                    },
                },
            });
        }
    }

    private async increaseXP(xp: number): Promise<void> {
        await ChatMessage.create({content: `Gained ${xp} Kingdom XP`});
        await this.saveKingdom({xp: this.getKingdom().xp + xp});
    }

    private async endTurn(): Promise<void> {
        const current = this.getKingdom();
        const capacity = getCapacity(this.game, current);
        await ChatMessage.create({
            content: `<h2>Ending Turn</h2>
            <ul>
                <li>Setting Resource Points to 0</li>
                <li>Reducing Effects' Turns by 1</li>
                <li>Setting available Supernatural and Creative Solutions to 0</li>
                <li>Adding values from the <b>next</b> columns to the <b>now</b> columns respecting their resource limits</li>
            </ul>
            `,
        });
        await this.game.pf2eKingmakerTools.migration.tickDownModifiers()
        await this.saveKingdom({
            fame: {
                ...current.fame,
                now: clamped(current.fame.next, 0, current.settings.maximumFamePoints),
                next: 0,
            },
            resourceDice: {
                now: current.resourceDice.next,
                next: 0,
            },
            supernaturalSolutions: 0,
            creativeSolutions: 0,
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

    private isOvercrowded(kingdom: Kingdom, settlement: SettlementAndScene): boolean {
        const structureStackMode = getStructureStackMode(kingdom);
        const autoCalculateSettlementLevel = kingdom.settings.autoCalculateSettlementLevel;
        const activities = getKingdomActivitiesById(this.game, this.getKingdom().homebrewActivities);
        const structures = getStructureResult(structureStackMode, autoCalculateSettlementLevel, activities, getStructuresByName(this.game), settlement);
        return getSettlementInfo(settlement, autoCalculateSettlementLevel).lots > structures.residentialLots;
    }

    private async adjustUnrest(): Promise<void> {
        const current = this.getKingdom();
        const unrest = await this.game.pf2eKingmakerTools.migration.adjustUnrest(current);
        await this.saveKingdom({
            unrest,
        });
    }

    private async checkForEvent(): Promise<void> {
        const kingdom = this.getKingdom();
        const rollMode = kingdom.settings.kingdomEventRollMode;
        const turnsWithoutEvent = kingdom.turnsWithoutEvent;
        const dc = this.calculateEventDC(turnsWithoutEvent);
        const roll = await (new Roll('1d20').roll());
        await roll.toMessage({flavor: `Checking for Event on DC ${dc}`}, {rollMode});
        if (roll.total >= dc) {
            await postChatMessage('An event occurs, roll a Kingdom Event!', rollMode);
            await this.saveKingdom({turnsWithoutEvent: 0});
        } else {
            await this.saveKingdom({turnsWithoutEvent: turnsWithoutEvent + 1});
        }
    }

    private async checkForCultEvent(): Promise<void> {
        const kingdom = this.getKingdom();
        const rollMode = kingdom.settings.kingdomEventRollMode;
        const turnsWithoutCultEvent = kingdom.turnsWithoutCultEvent;
        const dc = this.calculateCultEventDC(turnsWithoutCultEvent);
        const roll = await (new Roll('1d20').roll());
        await roll.toMessage({flavor: `Checking for Cult Event on DC ${dc}`}, {rollMode});
        if (roll.total >= dc) {
            await postChatMessage('An event occurs, roll a Cult Event!', rollMode);
            await this.saveKingdom({turnsWithoutCultEvent: 0});
        } else {
            await this.saveKingdom({turnsWithoutCultEvent: turnsWithoutCultEvent + 1});
        }
    }

    private async deleteKingdomPropertyAtIndex(ev: Event, property: keyof Kingdom): Promise<void> {
        const target = ev.currentTarget as HTMLButtonElement;
        const deleteIndex = target.dataset.deleteIndex;
        if (deleteIndex) {
            const deleteAt = parseInt(deleteIndex, 10);
            const values = [...this.getKingdom()[property] as unknown[]];
            values.splice(deleteAt, 1);
            await this.saveKingdom({
                [property]: values,
            });
        }
    }

    private async collectResources(): Promise<void> {
        const current = this.getKingdom();
        const resources = await this.game.pf2eKingmakerTools.migration.collectResources(current);
        await this.saveKingdom({
            resourcePoints: {
                now: resources.rp,
                next: current.resourcePoints.next,
            },
            resourceDice: {
                now: resources.rd,
                next: current.resourceDice.next,
            },
            commodities: {
                now: {
                    ore: resources.ore,
                    lumber: resources.lumber,
                    luxuries: resources.luxuries,
                    stone: resources.stone,
                    food: current.commodities.now.food,
                },
                next: current.commodities.next,
            },
        });
    }

    private getRuin(ruin: Ruin): object {
        return Object.fromEntries(Object.entries(ruin)
            .map(([ruin, values]) => [ruin, {
                label: capitalize(ruin),
                ...values,
                canReduce: values.value === 0 && values.penalty > 0,
            }]),
        );
    }

    private getWorkSites(sites: WorkSites): object {
        return {
            lumberCamps: {
                label: 'Lumber',
                total: sites.lumberCamps.quantity + sites.lumberCamps.resources,
                ...sites.lumberCamps,
            },
            mines: {
                label: 'Ore',
                total: sites.mines.quantity + sites.mines.resources,
                ...sites.mines,
            },
            quarries: {
                label: 'Stone',
                total: sites.quarries.quantity + sites.quarries.resources,
                ...sites.quarries,
            },
            luxurySources: {
                label: 'Luxuries',
                total: sites.luxurySources.quantity + sites.luxurySources.resources,
                ...sites.luxurySources,
            },
        };
    }

    private getCommodities(kingdom: Kingdom): object {
        const commodities = kingdom.commodities.now;
        const commoditiesNextRound = kingdom.commodities.next;
        const storageCapacity = getCapacity(this.game, kingdom);
        return Object.fromEntries((Object.entries(commodities) as [keyof Commodities, number][])
            .map(([commodity, value]) => [commodity, {
                label: capitalize(commodity),
                value: value,
                capacity: storageCapacity[commodity],
                next: commoditiesNextRound[commodity],
            }]),
        );
    }

    private async getLeaders(leaders: Leaders): Promise<object> {
        const kingdom = this.getKingdom();
        const leadershipBonuses = await this.game.pf2eKingmakerTools.migration.calculateLeadershipBonuses(kingdom);
        const entries = (Object.entries(leaders) as [keyof Leaders, LeaderValues][])
            .map(async ([leader, values]) => {
                const actor = values.uuid ? await fromUuid(values.uuid) as Actor | null : undefined;
                let bonus = 0;
                if (kingdom.settings.enableLeadershipModifiers) {
                    bonus = leadershipBonuses.get(leader as string) ?? 0
                }
                return [leader, {
                    label: capitalize(leader),
                    ...values,
                    img: actor?.img,
                    name: actor?.name,
                    level: actor?.level,
                    hasActor: !!actor,
                    bonus,
                }];
            });
        return Object.fromEntries(await Promise.all(entries));
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
        const allFeatsByName = new Map<string, KingdomFeat>();
        getAllFeats(this.game, this.getKingdom()).forEach(f => allFeatsByName.set(f.name, f));
        const levelFeats = [];
        const takenFeatsByLevel = Object.fromEntries(feats.map(feat => [feat.level, feat]));
        const noFeat = {
            name: '-',
            level: 0,
            text: '',
        };
        for (let featLevel = 2; featLevel <= kingdomLevel; featLevel += 2) {
            const existingFeat = takenFeatsByLevel[featLevel];
            if (existingFeat && allFeatsByName.has(existingFeat.id)) {
                levelFeats.push({...(allFeatsByName.get(existingFeat.id) ?? noFeat), takenAt: featLevel});
            } else {
                levelFeats.push({...noFeat, takenAt: featLevel});
            }
        }
        return {
            featIds: toLabelAndValue([...allFeatsByName.keys(), '-']),
            levelFeats: levelFeats,
            bonusFeats: bonusFeats
                .filter(feat => allFeatsByName.has(feat.id))
                .map(feat => allFeatsByName.get(feat.id)),
        };
    }

    private async reduceUnrest(): Promise<void> {
        const roll = await (new Roll('1d20').roll());
        await roll.toMessage({flavor: 'Reducing Unrest by 1 on an 11 or higher'});
        if (roll.total > 10) {
            await this.saveKingdom({
                unrest: Math.max(0, this.getKingdom().unrest - 1),
            });
        }
    }

    private calculateEventDC(turnsWithoutEvent: number): number {
        return Math.max(1, 16 - (turnsWithoutEvent * 5));
    }

    private calculateCultEventDC(turnsWithoutEvent: number): number {
        return Math.max(1, 20 - (turnsWithoutEvent * 2));
    }

    private calculateAnarchy(feats: Feat[], bonusFeats: BonusFeat[]): number {
        const increase = Math.max(0, ...getAllSelectedFeats(this.game, this.getKingdom()).map(f => f.increaseAnarchyLimit ?? 0));
        return increase + 20;
    }

    private async payConsumption(): Promise<void> {
        const current = this.getKingdom();
        const totalConsumption = getConsumption(this.game, current).current;
        const currentFood = current.commodities.now.food;
        const missingFood = totalConsumption - currentFood;
        const pay = missingFood * 5;
        if (totalConsumption > 0) {
            await ChatMessage.create({
                content: `<h2>Paying Consumption</h2>
            <ul>
                <li>Reducing food commodities by ${Math.min(currentFood, totalConsumption)}</li>
                ${missingFood > 0 ? `<li>Missing ${missingFood} food commodities. Either pay ${loseRP(pay)} or gain ${gainUnrest('1d4')}</li>` : ''}
            </ul>
            `,
            });
            await this.saveKingdom({
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
        await saveKingdom(this.sheetActor, kingdom);
    }

    private getKingdom(): Kingdom {
        return getKingdom(this.sheetActor);
    }

    private async claimedHexFeature(feature: 'landmark' | 'refuge'): Promise<void> {
        const current = this.getKingdom();
        if (feature === 'refuge') {
            await ChatMessage.create({content: 'Claimed a Refuge, please reduce a Ruin of your choice by 1'});
            await this.saveKingdom({
                modifiers: [...current.modifiers, {
                    name: 'Claimed Refuge',
                    enabled: true,
                    value: 2,
                    type: 'circumstance',
                    applyIf: [{"in": ["@ability", ['loyalty', 'stability']]}],
                    turns: 2,
                }],
            });
        } else {
            const unrestRoll = await (new Roll('1d4').roll());
            await unrestRoll.toMessage({flavor: 'Claimed a Landmark, reducing Unrest by:'});
            await this.saveKingdom({
                unrest: Math.max(0, current.unrest - unrestRoll.total),
                modifiers: [...current.modifiers, {
                    name: 'Claimed Landmark',
                    enabled: true,
                    value: 2,
                    type: 'circumstance',
                    applyIf: [{"in": ["@ability", ['culture', 'economy']]}],
                    turns: 2,
                }],
            });
        }
    }

    private async viewSettlementScene(ev: MouseEvent): Promise<void> {
        ev.preventDefault();
        ev.stopPropagation();
        const a = ev.currentTarget as HTMLElement;
        const id = a.dataset.id;
        const scene = this.game.scenes?.filter(scene => scene.id === id)?.[0];
        if (scene) {
            if (ev.ctrlKey) {
                await saveKingdom(this.sheetActor, {
                    activeSettlement: scene.id,
                });
                await scene.activate();
            } else {
                await scene.view();
            }
        }
    }

    private async showStructureBrowser(): Promise<void> {
        await showStructureBrowser(this.game, this.getKingdom(), this.sheetActor);
    }

    private async showTacticsBrowser(): Promise<void> {
        this.game.pf2eKingmakerTools.migration.tacticsBrowser(
            this.game, this.sheetActor, this.getKingdom()
        )
    }

    private async showArmyBrowser(): Promise<void> {
        this.game.pf2eKingmakerTools.migration.armyBrowser(
            this.game, this.sheetActor, this.getKingdom()
        )
    }
}


export async function showKingdom(game: Game): Promise<void> {
    const sheetActor = game?.actors?.find(a => a.name === 'Kingdom Sheet');
    if (sheetActor) {
        const obj = getKingdom(sheetActor);
        new KingdomApp(obj, {game, sheetActor}).render(true);
    } else {
        setupDialog(game, 'Kingdom', async () => {
            // eslint-disable-next-line @typescript-eslint/ban-ts-comment
            // @ts-ignore
            const sheetActor: Actor = await Actor.create({
                type: 'npc',
                name: 'Kingdom Sheet',
                img: 'icons/sundries/documents/document-sealed-red-yellow.webp',
            });
            await sheetActor?.setFlag('pf2e-kingmaker-tools', 'kingdom-sheet', getDefaultKingdomData(game));
            await showKingdom(game);
        });
    }
}

export const allLeaderTypes = ['pc', 'regularNpc', 'highlyMotivatedNpc', 'nonPathfinderNpc'] as const;
