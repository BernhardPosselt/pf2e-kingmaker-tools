import {KingdomFeat} from '../data/feats';
import {
    createLeadershipModifier,
    calculateModifiers,
    createActiveSettlementModifiers,
    getUntrainedProficiencyMode,
    Modifier,
    ModifierTotal,
    ModifierTotals,
    ModifierType,
    ModifierWithId,
} from '../modifiers';
import {allKingdomPhases, getActivitySkills, KingdomPhase} from '../data/activities';
import {Skill, skillAbilities} from '../data/skills';
import {createSkillModifiers, LeaderPerformingCheck} from '../skills';
import {getControlDC, hasFeat, Kingdom, SkillRanks} from '../data/kingdom';
import {capitalize, encodeJson, LabelAndValue, rollModeChoices, toLabelAndValue, unslugify} from '../../utils';
import {getKingdomActivitiesById, KingdomActivityById} from '../data/activityData';
import {cooperativeLeadership, rollCheck} from '../rolls';
import {
    ActiveSettlementStructureResult,
    getActiveSettlementStructureResult,
    getSettlement,
    getSettlementsWithoutLandBorders,
    getStolenLandsData,
} from '../scene';
import {DegreeOfSuccess} from '../../degree-of-success';
import {getArmyModifiers, getScoutingDC} from '../../armies/utils';
import {allLeaders, Leader} from "../data/leaders";

export type CheckType = 'skill' | 'activity';

export type AdditionalChatMessages = Partial<Record<DegreeOfSuccess, string>>[];

export interface CheckDialogFeatOptions {
    type: CheckType;
    activity?: string;
    dc?: number;
    dcAdjustment?: number;
    skill?: Skill;
    game: Game;
    kingdom: Kingdom;
    actor: Actor;
    overrideSkills?: Partial<SkillRanks>;
    onRoll: (consumeModifiers: Set<string>) => Promise<void>;
    afterRoll?: (degree: DegreeOfSuccess) => Promise<void>;
    additionalChatMessages?: Partial<Record<DegreeOfSuccess, string>>[];
}

interface CheckFormData {
    phase: KingdomPhase;
    dc: number;
    selectedSkill: Skill;
    customModifiers: CustomModifiers;
    overrideModifiers: Record<string, string>;
    consumeModifiers: Record<string, boolean>;
    rollMode: RollMode;
    leaderPerformingCheck: Leader;
}

interface TotalAndModifiers {
    total: ModifierTotals;
    modifiers: ModifierWithId[];
}

export interface ModifierBreakdown {
    name: string;
    type: ModifierType;
    value: number;
}

export interface ModifierBreakdowns {
    creativeSolution: ModifierBreakdown[],
    supernaturalSolution: ModifierBreakdown[],
    selected: ModifierBreakdown[],
}

type TotalFields = 'circumstance' | 'untyped' | 'item' | 'status';
type CustomModifiers = Pick<ModifierTotals, TotalFields>;

function createModifierList(modifiers: TotalAndModifiers): ModifierBreakdown[] {
    return modifiers.modifiers
        .filter(m => m.enabled)
        .map(m => {
            return {
                type: m.type,
                value: m.value,
                name: m.name,
            };
        });
}

export class CheckDialog extends FormApplication<FormApplicationOptions & CheckDialogFeatOptions, object, null> {
    private type: CheckType;
    private activity: string | undefined;
    private skill: Skill | undefined;
    private game: Game;
    private kingdom: Kingdom;
    private selectedSkill: Skill;
    private dc: number;
    private phase: KingdomPhase = 'event';
    private customModifiers: CustomModifiers = {
        item: {bonus: 0, penalty: 0},
        circumstance: {bonus: 0, penalty: 0},
        status: {bonus: 0, penalty: 0},
        untyped: {bonus: 0, penalty: 0},
    };
    private modifierOverrides: Record<string, boolean> = {};
    private consumeModifiers: Set<string> = new Set();
    private onRoll: (consumeModifiers: Set<string>) => Promise<void>;
    private actor: Actor;
    private overrideSkills: Partial<SkillRanks> | undefined;
    private afterRoll: (degree: DegreeOfSuccess) => Promise<void>;
    private rollOptions: string[] = [];
    private rollMode: RollMode = 'publicroll';
    private additionalChatMessages: AdditionalChatMessages;
    private leaderPerformingCheck: Leader = 'ruler';

    constructor(object: null, options: Partial<FormApplicationOptions> & CheckDialogFeatOptions) {
        super(object, options);
        this.type = options.type;
        this.activity = options.activity;
        this.skill = options.skill;
        this.game = options.game;
        this.kingdom = options.kingdom;
        this.actor = options.actor;
        this.additionalChatMessages = options.additionalChatMessages ?? [];
        this.onRoll = options.onRoll;
        this.afterRoll = options.afterRoll ?? (async (): Promise<void> => {
        });
        this.overrideSkills = options.overrideSkills;
        const automateResourceMode = this.kingdom.settings.automateResources;
        const {size: kingdomSize} = getStolenLandsData(this.game, automateResourceMode, this.kingdom);
        const controlDC = getControlDC(this.kingdom.level, kingdomSize, this.kingdom.leaders.ruler.vacant);
        if (this.type === 'skill') {
            this.selectedSkill = options.skill!;
            this.dc = options.dc ?? controlDC;
        } else {
            const activityData = getKingdomActivitiesById(this.kingdom.homebrewActivities);
            this.phase = activityData[this.activity!].phase;
            this.selectedSkill = this.getActivitySkills(options.kingdom.skillRanks, activityData)[0];
            const data = activityData[this.activity!];
            const activityDCType = data.dc;
            if (options.dc !== undefined) {
                this.dc = options.dc;
            } else if (activityDCType === 'control') {
                this.dc = controlDC;
            } else if (activityDCType === 'custom') {
                this.dc = 0;
            } else if (activityDCType === 'scouting') {
                this.dc = getScoutingDC(this.game);
            } else if (activityDCType === 'none') {
                throw Error('Can not perform activity with no DC');
            } else {
                this.dc = activityDCType;
            }
            // increase DC by adjustment if present
            this.dc = this.dc + (data.dcAdjustment ?? 0);
        }
    }

    static override get defaultOptions(): FormApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'kingdom-check';
        options.title = 'Skill Check';
        options.template = 'modules/pf2e-kingmaker-tools/templates/kingdom/check.hbs';
        options.submitOnChange = true;
        options.closeOnSubmit = false;
        options.classes = [];
        options.height = 'auto';
        return options;
    }

    override async getData(options?: Partial<FormApplicationOptions & { feats: KingdomFeat[] }>): Promise<object> {
        const activeSettlementStructureResult = getActiveSettlementStructureResult(this.game, this.kingdom);
        const activeSettlement = getSettlement(this.game, this.kingdom, this.kingdom.activeSettlement);
        const activities = getKingdomActivitiesById(this.kingdom.homebrewActivities);
        const applicableSkills = this.type === 'skill' ? [this.skill!] : this.getActivitySkills(this.kingdom.skillRanks, activities);
        const additionalModifiers: Modifier[] = createActiveSettlementModifiers(
            this.kingdom,
            activeSettlement?.settlement,
            activeSettlementStructureResult,
            getSettlementsWithoutLandBorders(this.game, this.kingdom),
        ).concat(getArmyModifiers(this.game));
        if (hasFeat(this.kingdom, 'Practical Magic (V&K)')) {
            const modifier: Modifier = {
                skills: ['engineering'],
                value: 1,
                type: 'status',
                name: 'Practical Magic',
                enabled: true,
            };
            if (this.kingdom.skillRanks.magic === 2) {
                additionalModifiers.push(modifier);
            } else if (this.kingdom.skillRanks.magic > 2) {
                additionalModifiers.push({
                    ...modifier,
                    value: 2,
                });
            }
        }
        if (this.kingdom.unrest > 0 && hasFeat(this.kingdom, 'Inspiring Entertainment')) {
            additionalModifiers.push({
                name: 'Inspiring Entertainment',
                type: 'circumstance',
                enabled: true,
                abilities: ['culture'],
                value: 2,
            });
        }
        const convertedCustomModifiers: Modifier[] = this.createCustomModifiers(this.customModifiers);
        const currentLeader = await this.findLeader();
        const skillModifiers = this.calculateModifiers(
            applicableSkills,
            activeSettlementStructureResult,
            additionalModifiers,
            convertedCustomModifiers,
            activities,
            currentLeader,
        );
        const creativeSolutionModifier = this.calculateModifiers(
            applicableSkills,
            activeSettlementStructureResult,
            [...additionalModifiers, {enabled: true, type: 'circumstance', value: 2, name: 'Creative Solution'}],
            convertedCustomModifiers,
            activities,
            currentLeader,
        )[this.selectedSkill];
        const supernaturalSolutionModifier = this.calculateModifiers(
            ['magic'],
            activeSettlementStructureResult,
            additionalModifiers,
            convertedCustomModifiers,
            activities,
            currentLeader,
        )['magic'];
        const selectedSkillModifier = skillModifiers[this.selectedSkill];
        // set all modifiers as consumed that have a consumeId and are enabled
        this.rollOptions = selectedSkillModifier.total.rollOptions;
        this.consumeModifiers = new Set(selectedSkillModifier.modifiers
            .filter(modifier => modifier.enabled && modifier.consumeId !== undefined)
            .map(modifier => modifier.consumeId!));
        const modifierBreakdown = encodeJson({
            creativeSolution: createModifierList(creativeSolutionModifier),
            supernaturalSolution: createModifierList(supernaturalSolutionModifier),
            selected: createModifierList(selectedSkillModifier),
        });
        return {
            ...(await super.getData(options)),
            invalid: this.selectedSkill === undefined,
            dc: this.dc,
            title: this.activity ? unslugify(this.activity) : capitalize(this.skill!),
            activity: this.activity,
            selectableSkills: this.createSelectableSkills(skillModifiers),
            selectedSkill: this.selectedSkill,
            modifiers: this.createModifiers(selectedSkillModifier),
            phase: this.phase,
            customModifiers: this.customModifiers,
            creativeSolutionModifier: creativeSolutionModifier.total.value,
            supernaturalSolutionModifier: supernaturalSolutionModifier.total.value,
            modifierBreakdown,
            rollMode: this.rollMode,
            rollModeChoices: rollModeChoices,
            phases: toLabelAndValue([...allKingdomPhases], {capitalizeLabel: true}),
            overrides: toLabelAndValue(['enabled', 'disabled'], {emptyChoice: '-', capitalizeLabel: true}),
            useLeadershipModifiers: this.kingdom.settings.enableLeadershipModifiers,
            leaderPerformingCheck: this.leaderPerformingCheck,
            leaders: allLeaders.map(l => {
                return {label: capitalize(l), value: l}
            }),
        };
    }

    private async findLeader(): Promise<LeaderPerformingCheck | undefined> {
        const leader = this.kingdom.leaders[this.leaderPerformingCheck];
        const uuid = leader.uuid;
        if (uuid) {
            const actor = await fromUuid(uuid) as Actor | null;
            if (actor) {
                return {
                    position: this.leaderPerformingCheck,
                    type: leader.type,
                    level: actor.level,
                    skillRanks: Object.fromEntries(Object.entries(actor.skills).map(([key, value]) => {
                        return [key, value.rank];
                    })),
                }
            }
        }
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        const $html = html[0];
        $html.querySelector('#km-roll-skill-assurance')?.addEventListener('click', async (event) => {
            const target = event.currentTarget as HTMLButtonElement;
            const dc = parseInt(target.dataset.dc ?? '0', 10);
            const modifier = parseInt(target.dataset.modifier ?? '0', 10);
            const creativeSolutionModifier = parseInt(target.dataset.creativeSolutionModifier ?? '0', 10);
            const supernaturalSolutionModifier = parseInt(target.dataset.supernaturalSolutionModifier ?? '0', 10);
            const activity = target.dataset.activity;
            const label = target.dataset.type!;
            const skill = target.dataset.skill as Skill;
            const formula = `${modifier}`;

            const degree = await rollCheck({
                formula,
                label,
                activity: activity ? getKingdomActivitiesById(this.kingdom.homebrewActivities)[activity] : undefined,
                dc,
                skill,
                modifier,
                actor: this.actor,
                adjustDegreeOfSuccess: cooperativeLeadership(
                    hasFeat(this.kingdom, 'Cooperative Leadership'),
                    this.kingdom.level,
                    this.kingdom.skillRanks[skill],
                    [...this.rollOptions],
                ),
                rollOptions: this.rollOptions,
                creativeSolutionModifier,
                supernaturalSolutionModifier,
                rollType: 'selected',
                rollMode: this.rollMode,
                additionalChatMessages: this.additionalChatMessages,
            });
            await this.onRoll(this.consumeModifiers);
            await this.afterRoll(degree);
            await this.close();
        });

        $html.querySelector('#km-roll-skill')?.addEventListener('click', async (event) => {
            const target = event.currentTarget as HTMLButtonElement;
            const dc = parseInt(target.dataset.dc ?? '0', 10);
            const modifier = parseInt(target.dataset.modifier ?? '0', 10);
            const activity = target.dataset.activity;
            const label = target.dataset.type!;
            const modifierBreakdown = target.dataset.modifierBreakdown as string;
            const skill = target.dataset.skill as Skill;
            const creativeSolutionModifier = parseInt(target.dataset.creativeSolutionModifier ?? '0', 10);
            const supernaturalSolutionModifier = parseInt(target.dataset.supernaturalSolutionModifier ?? '0', 10);

            const formula = `1d20+${modifier}`;
            const degree = await rollCheck({
                formula,
                label,
                activity: activity ? getKingdomActivitiesById(this.kingdom.homebrewActivities)[activity] : undefined,
                dc,
                skill,
                modifier,
                modifierBreakdown,
                actor: this.actor,
                adjustDegreeOfSuccess: cooperativeLeadership(
                    hasFeat(this.kingdom, 'Cooperative Leadership'),
                    this.kingdom.level,
                    this.kingdom.skillRanks[skill],
                    [...this.rollOptions],
                ),
                rollOptions: this.rollOptions,
                creativeSolutionModifier,
                supernaturalSolutionModifier,
                rollType: 'selected',
                rollMode: this.rollMode,
                additionalChatMessages: this.additionalChatMessages,
            });
            await this.onRoll(this.consumeModifiers);
            await this.afterRoll(degree);
            await this.close();
        });
    }

    /* eslint-disable @typescript-eslint/no-explicit-any */
    protected async _updateObject(event: Event, formData: any): Promise<void> {
        const data = foundry.utils.expandObject(formData) as CheckFormData;
        console.log(data);
        this.selectedSkill = data.selectedSkill;
        this.leaderPerformingCheck = data.leaderPerformingCheck;
        this.dc = data.dc;
        this.phase = data.phase;
        this.rollMode = data.rollMode;
        this.customModifiers = this.homogenize(data.customModifiers);
        this.modifierOverrides = (Object.entries(data.overrideModifiers ?? {}) as [string, string][])
            .filter(([, state]) => state !== '-')
            .map(([id, state]) => {
                return {[id]: state === 'enabled'};
            })
            .reduce((a, b) => Object.assign(a, b), {});
        this.render();
    }

    private getActivitySkills(ranks: SkillRanks, activities: KingdomActivityById): Skill[] {
        const activity = this.activity!;
        const expandMagicActivities = new Set([
            'celebrate-holiday', 'celebrate-holiday-vk', 'craft-luxuries', 'create-a-masterpiece', 'rest-and-relax'
        ]);
        const ignoreSkillRequirements = this.kingdom.settings.kingdomIgnoreSkillRequirements;
        const skillRankFilters = ignoreSkillRequirements ? undefined : ranks;
        const activitySkills = getActivitySkills(this.overrideSkills ?? activities[activity].skills, skillRankFilters);
        const practicalMagic: Skill[] = activitySkills.includes('engineering') && hasFeat(this.kingdom, 'Practical Magic') ? ['magic'] : [];
        const expandMagicSkills: Skill[] = this.kingdom.settings.expandMagicUse && expandMagicActivities.has(activity) ? ['magic'] : [];
        return Array.from(new Set([...activitySkills, ...expandMagicSkills, ...practicalMagic]));
    }

    private calculateModifiers(
        applicableSkills: Skill[],
        activeSettlementStructureResult: ActiveSettlementStructureResult | undefined,
        additionalModifiers: Modifier[],
        convertedCustomModifiers: Modifier[],
        activities: KingdomActivityById,
        currentLeader: LeaderPerformingCheck | undefined,
    ): Record<Skill, TotalAndModifiers> {
        return Object.fromEntries(applicableSkills.map(skill => {
            const modifiers = createSkillModifiers({
                ruin: this.kingdom.ruin,
                unrest: this.kingdom.unrest,
                skillRank: (this.kingdom.skillRanks)[skill],
                abilityScores: this.kingdom.abilityScores,
                leaders: this.kingdom.leaders,
                kingdomLevel: this.kingdom.level,
                untrainedProficiencyMode: getUntrainedProficiencyMode(this.kingdom),
                ability: skillAbilities[skill],
                skillItemBonus: activeSettlementStructureResult?.skillBonuses?.[skill],
                additionalModifiers: [...additionalModifiers, ...convertedCustomModifiers],
                activity: this.activity,
                phase: this.phase,
                skill,
                overrides: this.modifierOverrides,
                activities,
                useLeadershipModifiers: this.kingdom.settings.enableLeadershipModifiers,
                leaderSkills: this.kingdom.settings.leaderSkills,
                leaderKingdomSkills: this.kingdom.settings.leaderKingdomSkills,
                currentLeader,
            });
            const total = calculateModifiers(modifiers);
            return [skill, {total, modifiers}];
        })) as Record<Skill, TotalAndModifiers>;
    }

    private createSelectableSkills(skillModifiers: Record<Skill, TotalAndModifiers>): LabelAndValue[] {
        return Object.entries(skillModifiers).map(([skill, data]) => {
            const value = data.total.value;
            const modifier = value >= 0 ? `+${value}` : value;
            return {label: `${capitalize(skill)} ${modifier}`, value: skill};
        });
    }

    private createCustomModifiers(customModifiers: CustomModifiers): Modifier[] {
        return (Object.entries(customModifiers) as [TotalFields, ModifierTotal][])
            .flatMap(([type, values]) => {
                return [{
                    value: values.bonus,
                    type,
                    name: capitalize(type) + ' Bonus',
                    enabled: true,
                }, {
                    value: -values.penalty,
                    type,
                    name: capitalize(type) + ' Penalty',
                    enabled: true,
                }];
            });
    }

    private createModifiers(skillModifier: TotalAndModifiers | undefined): object | undefined {
        if (skillModifier) {
            const total = skillModifier.total.value;
            const totalLabel = total >= 0 ? `+${total}` : total;
            return {
                total,
                totalLabel,
                assurance: skillModifier.total.assurance,
                modifiers: skillModifier.modifiers.map(modifier => {
                    const type = capitalize(modifier.type);
                    const override = this.modifierOverrides[modifier.id];
                    return {
                        name: modifier.name,
                        type: modifier.value < 0 ? `${type} Penalty` : `${type} Bonus`,
                        value: modifier.value,
                        enabled: modifier.enabled,
                        override: override === undefined ? '-' : (override ? 'enabled' : 'disabled'),
                        id: modifier.id,
                        consumable: modifier.consumeId !== undefined &&
                            this.consumeModifiers.has(modifier.consumeId),
                    };
                }),
            };
        }
    }

    /**
     * Make all bonuses and penalties positive
     * @param customModifiers
     * @private
     */
    private homogenize(customModifiers: CustomModifiers): CustomModifiers {
        return Object.fromEntries((Object.entries(customModifiers) as [TotalFields, ModifierTotal][])
            .map(([type, values]) => {
                const x: [TotalFields, ModifierTotal] = [type, {
                    bonus: Math.abs(values.bonus),
                    penalty: Math.abs(values.penalty),
                }];
                return x;
            })) as CustomModifiers;
    }
}
