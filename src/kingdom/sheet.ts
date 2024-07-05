import {getBooleanSetting, getNumberSetting, getStringSetting} from '../settings';
import {
    allFameTypes,
    allHeartlands,
    BonusFeat,
    Commodities,
    FameType,
    Feat,
    getControlDC,
    getDefaultKingdomData,
    getLevelData,
    getSizeData,
    hasFeat,
    Kingdom,
    Leaders,
    LeaderValues,
    ResourceDieSize,
    Ruin,
    Settlement,
    WorkSites,
} from './data/kingdom';
import {
    capitalize,
    clamped,
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
    getAllSettlements,
    getCurrentScene,
    getScene,
    getSettlement,
    getSettlementInfo,
    getSettlementsWithoutLandBorders,
    getStolenLandsData,
    getStructureResult,
    getStructureStackMode,
    ResourceAutomationMode,
    SettlementAndScene,
} from './scene';
import {allFeats, allFeatsByName} from './data/feats';
import {addGroupDialog} from './dialogs/add-group-dialog';
import {AddBonusFeatDialog} from './dialogs/add-bonus-feat-dialog';
import {addOngoingEventDialog} from './dialogs/add-ongoing-event-dialog';
import {rollCultEvent, rollKingdomEvent} from '../kingdom-events';
import {calculateEventXP, calculateHexXP, calculateRpXP} from './xp';
import {setupDialog} from './dialogs/setup-dialog';
import {featuresByLevel, uniqueFeatures} from './data/features';
import {
    allCompanions,
    applyLeaderCompanionRules,
    getCompanionUnlockActivities,
    getOverrideUnlockCompanionNames,
} from './data/companions';
import {
    createActivityLabel,
    enableCompanionActivities,
    getPerformableActivities,
    groupKingdomActivities,
} from './data/activities';
import {AbilityScores, calculateAbilityModifier} from './data/abilities';
import {calculateSkills} from './skills';
import {calculateInvestedBonus, isInvested} from './data/leaders';
import {CheckDialog} from './dialogs/check-dialog';
import {Skill} from './data/skills';
import {showHelpDialog} from './dialogs/show-help-dialog';
import {showSettlement} from './dialogs/settlement';
import {createActiveSettlementModifiers, getUntrainedProficiencyMode, Modifier, modifierToLabel} from './modifiers';
import {addEffectDialog} from './dialogs/add-effect-dialog';
import {getKingdom, saveKingdom} from './storage';
import {gainFame, getCapacity, getConsumption} from './kingdom-utils';
import {calculateUnrestPenalty} from './data/unrest';
import {editSettlementDialog} from './dialogs/edit-settlement-dialog';
import {showKingdomSettings} from './dialogs/kingdom-settings';
import {openJournal} from '../foundry-utils';
import {showStructureBrowser} from './dialogs/structure-browser';
import {gainUnrest, getKingdomActivitiesById, loseRP} from './data/activityData';
import {manageKingdomActivitiesDialog} from './dialogs/activities-dialog';
import {kingdomSizeDialog} from './dialogs/kingdom-size-dialog';
import {settlementSizeDialog} from './dialogs/settlement-size-dialog';
import {getSelectedArmies} from '../armies/utils';
import {showArmyTacticsBrowser} from './dialogs/army-tactics-browser';
import {showArmyBrowser} from './dialogs/army-browser';

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

function createEffects(modifiers: Modifier[]): Effect[] {
    return modifiers.map(modifier => {
        return {
            name: modifier.name,
            effect: modifierToLabel(modifier),
            turns: modifier.turns === undefined ? 'indefinite' : `${modifier.turns}`,
            consumable: modifier.consumeId !== undefined,
        };
    });
}


class KingdomApp extends FormApplication<FormApplicationOptions & KingdomOptions, object, Kingdom> {
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

    private sheetActor: Actor;
    private readonly game: Game;
    private nav: KingdomTab = 'turn';

    constructor(object: Kingdom, options: Partial<FormApplicationOptions> & KingdomOptions) {
        super(object, options);
        this.game = options.game;
        this.sheetActor = options.sheetActor;
        this.sheetActor.apps[this.appId] = this;
    }

    override async getData(): Promise<object> {
        const isGM = this.game.user?.isGM ?? false;
        const kingdomData = this.getKingdom();
        this.object = kingdomData;
        const automateResourceMode = getStringSetting(this.game, 'automateResources') as ResourceAutomationMode;
        const autoCalculateArmyConsumption = getBooleanSetting(this.game, 'autoCalculateArmyConsumption');
        const {size: kingdomSize, workSites} = getStolenLandsData(this.game, automateResourceMode, kingdomData);
        const sizeData = getSizeData(kingdomSize);
        const autoCalculateSettlementLevel = getBooleanSetting(this.game, 'autoCalculateSettlementLevel');
        const {
            leadershipActivityNumber,
            settlementConsumption,
            unlockedActivities: unlockedSettlementActivities,
        } = getAllMergedSettlements(this.game, kingdomData);
        const {current: totalConsumption, surplus: farmSurplus} = getConsumption(this.game, kingdomData);
        const useXpHomebrew = getBooleanSetting(this.game, 'vanceAndKerensharaXP');
        const homebrewSkillIncreases = getBooleanSetting(this.game, 'kingdomSkillIncreaseEveryLevel');
        const activeSettlementStructureResult = getActiveSettlementStructureResult(this.game, kingdomData);
        const activeSettlement = getSettlement(this.game, kingdomData, kingdomData.activeSettlement);

        const unlockedActivities = new Set<string>([
            ...unlockedSettlementActivities,
            ...getCompanionUnlockActivities(kingdomData.leaders, getOverrideUnlockCompanionNames(this.game)),
        ]);
        const hideActivities = kingdomData.activityBlacklist
            .map(activity => {
                return {[activity]: true};
            })
            .reduce((a, b) => Object.assign(a, b), {});
        const ignoreSkillRequirements = getBooleanSetting(this.game, 'kingdomIgnoreSkillRequirements');
        const activities = getKingdomActivitiesById(kingdomData.homebrewActivities);
        const enabledActivities = getPerformableActivities(
            kingdomData.skillRanks,
            activeSettlementStructureResult?.active?.allowCapitalInvestment === true
            || (activeSettlement?.settlement.type === 'capital' && getBooleanSetting(this.game, 'capitalInvestmentInCapital')),
            ignoreSkillRequirements,
            activities,
        );
        const groupedActivities = groupKingdomActivities(activities);
        const currentSceneId = getCurrentScene(this.game)?.id;
        const canAddSettlement = kingdomData.settlements.find(settlement => settlement.sceneId === currentSceneId) === undefined;
        const canAddRealm = isNonNullable(currentSceneId) && currentSceneId !== kingdomData.realmSceneId;
        const structureStackMode = getStructureStackMode(this.game);
        const automateResources = automateResourceMode !== 'manual';
        const showAddRealmButton = isGM && automateResourceMode === 'tileBased';
        const showRealmData = automateResourceMode === 'kingmaker'
            || automateResourceMode === 'manual'
            || (isNonNullable(kingdomData.realmSceneId) && this.game.scenes?.find(s => s.id === kingdomData.realmSceneId) !== undefined);
        console.log(kingdomData);
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
            unrestPenalty: calculateUnrestPenalty(kingdomData.unrest),
            anarchyAt: this.calculateAnarchy(kingdomData.feats, kingdomData.bonusFeats),
            resourceDieSize: sizeData.resourceDieSize,
            resourceDiceNum: this.getResourceDiceNum(kingdomData),
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
            skills: calculateSkills({
                ruin: kingdomData.ruin,
                skillRanks: kingdomData.skillRanks,
                leaders: kingdomData.leaders,
                abilityScores: kingdomData.abilityScores,
                unrest: kingdomData.unrest,
                kingdomLevel: kingdomData.level,
                untrainedProficiencyMode: getUntrainedProficiencyMode(this.game),
                skillItemBonuses: activeSettlementStructureResult?.merged?.skillBonuses,
                additionalModifiers: createActiveSettlementModifiers(
                    kingdomData,
                    activeSettlement?.settlement,
                    activeSettlementStructureResult,
                    getSettlementsWithoutLandBorders(this.game, kingdomData),
                ),
                activities,
            }),
            leaders: this.getLeaders(kingdomData.leaders),
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
            actorTypes: [
                {label: 'PC', value: 'pc'},
                {label: 'NPC', value: 'npc'},
                {label: 'Companion', value: 'companion'},
            ],
            companions: toLabelAndValue([...allCompanions]),
            fameValues: toLabelAndValue(range(0, 4)),
            aspirations: [{value: 'famous', label: 'Fame'}, {value: 'infamous', label: 'Infamy'}],
            settlements: kingdomData.settlements
                .map(settlement => getSettlement(this.game, kingdomData, settlement.sceneId))
                .filter(settlement => settlement !== undefined)
                .map(settlement => {
                    const s = settlement as SettlementAndScene;
                    const waterBorders = s.settlement.waterBorders ?? 0;
                    const structureResult = getStructureResult(structureStackMode, autoCalculateSettlementLevel, activities, s);
                    return {
                        ...s.settlement,
                        ...getSettlementInfo(s, autoCalculateSettlementLevel),
                        waterBorders,
                        overcrowded: this.isOvercrowded(s),
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
            leadershipActivities: enableCompanionActivities('leadership', unlockedActivities, groupedActivities)
                .map(activity => {
                    return {label: createActivityLabel(groupedActivities, activity, kingdomData), value: activity};
                })
                .sort((a, b) => a.label.localeCompare(b.label)),
            regionActivities: enableCompanionActivities('region', unlockedActivities, groupedActivities)
                .map(activity => {
                    return {label: createActivityLabel(groupedActivities, activity, kingdomData), value: activity};
                }),
            armyActivities: enableCompanionActivities('army', unlockedActivities, groupedActivities)
                .map(activity => {
                    return {label: createActivityLabel(groupedActivities, activity, kingdomData), value: activity};
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
            turnsWithoutCultEvent: kingdomData.turnsWithoutCultEvent,
            cultEventDC: this.calculateCultEventDC(kingdomData.turnsWithoutCultEvent),
            civicPlanning: kingdomData.level >= 12,
            useXpHomebrew,
            canAddSettlement,
            effects: createEffects(kingdomData.modifiers),
            cultOfTheBloomEvents: getBooleanSetting(this.game, 'cultOfTheBloomEvents') && isGM,
            automateResources,
            canAddRealm,
            showRealmData,
            showAddRealmButton,
        };
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
        const $html = html[0];
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
                kingdomSizeDialog();
            });
        $html.querySelector('#km-settlement-size-help')
            ?.addEventListener('click', async (ev) => {
                ev.stopPropagation();
                ev.preventDefault();
                settlementSizeDialog();
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
            ?.addEventListener('click', async () => await rollKingdomEvent(this.game));
        $html.querySelector('#km-roll-cult-event')
            ?.addEventListener('click', async () => await rollCultEvent(this.game));
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
            ?.addEventListener('click', async () => addOngoingEventDialog((name) => {
                const current = this.getKingdom();
                this.saveKingdom({
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
                    const target = ev.currentTarget as HTMLButtonElement;
                    const modifier = parseInt(target.dataset.modifier ?? '0', 10);
                    await this.increaseXP(calculateEventXP(modifier));
                });
            });
        $html.querySelectorAll('.km-claimed-hexes-xp')
            ?.forEach(el => {
                el.addEventListener('click', async (ev) => {
                    const target = ev.currentTarget as HTMLButtonElement;
                    const hexes = parseInt(target.dataset.hexes ?? '0', 10);
                    const current = this.getKingdom();
                    const automateResourceMode = getStringSetting(this.game, 'automateResources') as ResourceAutomationMode;
                    const {size: kingdomSize} = getStolenLandsData(this.game, automateResourceMode, current);
                    const useHomeBrew = getBooleanSetting(this.game, 'vanceAndKerensharaXP');
                    await this.increaseXP(calculateHexXP({
                        hexes,
                        kingdomSize,
                        useVK: useHomeBrew,
                        xpPerClaimedHex: getNumberSetting(this.game, 'xpPerClaimedHex'),
                    }));
                });
            });
        $html.querySelector('#km-rp-to-xp')
            ?.addEventListener('click', async () => {
                const current = this.getKingdom();
                const useHomeBrew = getBooleanSetting(this.game, 'vanceAndKerensharaXP');
                await this.increaseXP(calculateRpXP({
                    useVK: useHomeBrew,
                    kingdomLevel: current.level,
                    rpToXpConversionLimit: getNumberSetting(this.game, 'rpToXpConversionLimit'),
                    rpToXpConversionRate: getNumberSetting(this.game, 'rpToXpConversionRate'),
                    rp: current.resourcePoints.now,
                }));
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
                addGroupDialog((group) => this.saveKingdom({
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
                    const activityData = getKingdomActivitiesById(kingdom.homebrewActivities);
                    if (activityData[activity].dc === 'none') {
                        await showHelpDialog(this.game, this.sheetActor, activity);
                    } else {
                        new CheckDialog(null, {
                            activity,
                            kingdom: kingdom,
                            game: this.game,
                            type: 'activity',
                            onRoll: this.consumeModifiers.bind(this),
                            actor: this.sheetActor,
                        }).render(true);
                    }
                });
            });
        $html.querySelectorAll('.kingdom-skill')
            ?.forEach(el => {
                el.addEventListener('click', async (el) => {
                    const target = el.currentTarget as HTMLButtonElement;
                    const skill = target.dataset.skill;
                    new CheckDialog(null, {
                        kingdom: this.getKingdom(),
                        game: this.game,
                        skill: skill as Skill,
                        type: 'skill',
                        onRoll: this.consumeModifiers.bind(this),
                        actor: this.sheetActor,
                    }).render(true);
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
                const allActivities = Object.keys(getKingdomActivitiesById(this.getKingdom().homebrewActivities));
                addEffectDialog(allActivities, async (modifier) => {
                    const current = this.getKingdom();
                    current.modifiers.push(modifier);
                    await this.saveKingdom(current);
                });
            });
        $html.querySelectorAll('.km-delete-effect')
            ?.forEach(el => {
                el.addEventListener('click', async (ev) => await this.deleteKingdomPropertyAtIndex(ev, 'modifiers'));
            });
        $html.querySelector('.kingdom-settings')?.addEventListener('click', () => showKingdomSettings({
            game: this.game,
            sheetActor: this.sheetActor,
            onSave: () => {
                this.render();
            },
        }));
        $html.querySelectorAll('.edit-settlement')
            ?.forEach(el => {
                el.addEventListener('click', async (ev) => {
                    const target = ev?.currentTarget as HTMLButtonElement | null;
                    const index = parseInt(target?.dataset?.index as string, 10);
                    const current = this.getKingdom();
                    const data = current.settlements[index];
                    const name = getScene(this.game, data.sceneId)?.name as string;
                    const autoLevel = getBooleanSetting(this.game, 'autoCalculateSettlementLevel');
                    editSettlementDialog(autoLevel, name, data, (savedData) => {
                        current.settlements[index] = savedData;
                        this.saveKingdom({
                            settlements: current.settlements,
                        });
                    });
                });
            });
    }

    private async upkeepGainFame(): Promise<void> {
        await ChatMessage.create({content: 'Gaining 1 Fame'});
        await this.saveKingdom(gainFame(this.getKingdom(), 1));
    }

    private async consumeModifiers(consumeIds: Set<string>): Promise<void> {
        const current = this.getKingdom();
        await this.saveKingdom({
            modifiers: current.modifiers
                .filter(modifier => {
                    const id = modifier.consumeId;
                    if (id === undefined || id === null) {
                        return true;
                    } else {
                        return !consumeIds.has(id);
                    }
                }),
        });
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
        // tick down modifiers
        const modifiers = current.modifiers
            .map(modifier => {
                const turns = modifier.turns === undefined ? undefined : modifier.turns - 1;
                return {
                    ...modifier,
                    turns: turns,
                };
            })
            .filter(modifier => (modifier?.turns ?? 1) > 0);
        await this.saveKingdom({
            modifiers,
            fame: {
                ...current.fame,
                now: clamped(current.fame.next, 0, 3),
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

    private isOvercrowded(settlement: SettlementAndScene): boolean {
        const structureStackMode = getStructureStackMode(this.game);
        const autoCalculateSettlementLevel = getBooleanSetting(this.game, 'autoCalculateSettlementLevel');
        const activities = getKingdomActivitiesById(this.getKingdom().homebrewActivities);
        const structures = getStructureResult(structureStackMode, autoCalculateSettlementLevel, activities, settlement);
        return getSettlementInfo(settlement, autoCalculateSettlementLevel).lots > structures.residentialLots;
    }

    private async adjustUnrest(): Promise<void> {
        const current = this.getKingdom();
        const data = getAllSettlements(this.game, current);
        const overcrowdedSettlements = data.filter(s => this.isOvercrowded(s)).length;
        const secondaryTerritories = data.some(s => s.settlement.secondaryTerritory) ? 1 : 0;
        const atWar = current.atWar ? 1 : 0;
        let rulerVacancyUnrest = 0;
        if (current.leaders.ruler.vacant) {
            const roll = await (new Roll('1d4').roll());
            await roll.toMessage({flavor: 'Gaining Unrest because Leader is vacant'});
            rulerVacancyUnrest = roll.total;
        }
        const newUnrest = atWar + overcrowdedSettlements + secondaryTerritories + rulerVacancyUnrest;
        let unrest = newUnrest + current.unrest;
        if (current.level >= 20 && unrest > 0) {
            unrest = 0;
            await ChatMessage.create({content: 'Ignoring any Unrest increase due to "Envy of the World" Kingdom Feature'});
        } else {
            await ChatMessage.create({
                content: `<h2>Gaining Unrest</h2> 
                <ul>
                    <li><b>Overcrowded Settlements</b>: ${overcrowdedSettlements}</li>
                    <li><b>Secondary Territories</b>: ${secondaryTerritories}</li>
                    <li><b>Kingdom At War</b>: ${atWar}</li>
                    <li><b>Ruler Vacancy Penalty</b>: ${rulerVacancyUnrest}</li>
                    <li><b>Total</b>: ${newUnrest}</li>
                </ul>
                `,
            });
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
        await this.saveKingdom({
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
            await postChatMessage('An event occurs, roll a Kingdom Event!', rollMode);
            await this.saveKingdom({turnsWithoutEvent: 0});
        } else {
            await this.saveKingdom({turnsWithoutEvent: turnsWithoutEvent + 1});
        }
    }

    private async checkForCultEvent(): Promise<void> {
        const rollMode = getStringSetting(this.game, 'kingdomEventRollMode') as unknown as keyof CONFIG.Dice.RollModes;
        const turnsWithoutCultEvent = this.getKingdom().turnsWithoutCultEvent;
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
        const automateResourceMode = getStringSetting(this.game, 'automateResources') as ResourceAutomationMode;
        const {size: kingdomSize, workSites} = getStolenLandsData(this.game, automateResourceMode, current);
        const sizeData = getSizeData(kingdomSize);
        const capacity = getCapacity(this.game, current);
        const dice = this.getResourceDiceNum(current);
        const rolledPoints = await this.rollResourceDice(sizeData.resourceDieSize, dice);
        const commodities = this.calculateCommoditiesThisTurn(workSites);
        await ChatMessage.create({
            content: `
        <h2>Collecting Resources</h2>
        <ul>
            <li><b>Resource Points</b>: ${rolledPoints}</li>
            <li><b>Ore</b>: ${commodities.ore}</li>
            <li><b>Lumber</b>: ${commodities.lumber}</li>
            <li><b>Stone</b>: ${commodities.stone}</li>
            <li><b>Luxuries</b>: ${commodities.luxuries}</li>
        </ul>
        `,
        });
        await this.saveKingdom({
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
                    ore: Math.min(capacity.ore, current.commodities.now.ore + commodities.ore),
                    lumber: Math.min(capacity.lumber, current.commodities.now.lumber + commodities.lumber),
                    luxuries: Math.min(capacity.luxuries, current.commodities.now.luxuries + commodities.luxuries),
                    stone: Math.min(capacity.stone, current.commodities.now.stone + commodities.stone),
                    food: current.commodities.now.food,
                },
                next: current.commodities.next,
            },
        });
    }

    private getResourceDiceNum(kingdom: Kingdom): number {
        const featDice = hasFeat(kingdom, 'Insider Trading') ? 1 : 0;
        const levelData = getLevelData(kingdom.level);
        return Math.max(0, levelData.resourceDice + kingdom.resourceDice.now + featDice);
    }


    private calculateCommoditiesThisTurn(sites: WorkSites): Omit<Commodities, 'food'> {
        return {
            ore: sites.mines.quantity + sites.mines.resources,
            lumber: sites.lumberCamps.quantity + sites.lumberCamps.resources,
            luxuries: sites.luxurySources.quantity + sites.luxurySources.resources,
            stone: sites.quarries.quantity + sites.quarries.resources,
        };
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
        return super.close(options);
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

    private getLeaders(leaders: Leaders): object {
        const appliedLeaders = applyLeaderCompanionRules(leaders);
        return Object.fromEntries((Object.entries(appliedLeaders) as [keyof Leaders, LeaderValues][])
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
            featIds: toLabelAndValue(Object.keys(allFeatsByName)),
            levelFeats: levelFeats,
            bonusFeats: bonusFeats
                .filter(feat => feat.id in allFeatsByName)
                .map(feat => allFeatsByName[feat.id]),
        };
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
        const hasEndureAnarchy = feats.some(f => f.id === 'Endure Anarchy') ||
            bonusFeats.some(f => f.id === 'Endure Anarchy');
        return hasEndureAnarchy ? 24 : 20;
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
                    abilities: ['loyalty', 'stability'],
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
                    abilities: ['culture', 'economy'],
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
        await showStructureBrowser(this.game, this.getKingdom(), this.sheetActor, this.consumeModifiers.bind(this));
    }

    private async showTacticsBrowser(): Promise<void> {
        const armies = getSelectedArmies(this.game);
        if (armies.length !== 1) {
            ui.notifications?.error('Please target a single army on the scene (<i class="fa-solid fa-keyboard"></i> <b>t</b>)');
        } else {
            const army = armies[0];
            await showArmyTacticsBrowser({
                game: this.game,
                army,
                kingdom: this.getKingdom(),
                onRoll: this.consumeModifiers.bind(this),
                sheetActor: this.sheetActor,
            });
        }
    }

    private async showArmyBrowser(): Promise<void> {
        await showArmyBrowser({
            game: this.game,
            kingdom: this.getKingdom(),
            onRoll: this.consumeModifiers.bind(this),
            sheetActor: this.sheetActor,
        });
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
            await sheetActor?.setFlag('pf2e-kingmaker-tools', 'kingdom-sheet', getDefaultKingdomData());
            await showKingdom(game);
        });
    }
}
