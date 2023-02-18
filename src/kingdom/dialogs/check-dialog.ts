import {KingdomFeat} from '../data/feats';
import {
    calculateModifiers,
    createAdditionalModifiers,
    Modifier,
    ModifierTotal,
    ModifierTotals,
    ModifierWithId,
} from '../modifiers';
import {Activity, getActivityPhase, getActivitySkills, KingdomPhase} from '../data/activities';
import {Skill, skillAbilities} from '../data/skills';
import {createSkillModifiers} from '../skills';
import {getBooleanSetting} from '../../settings';
import {getMergedData, getSettlementScene} from '../scene';
import {getControlDC, Kingdom, SkillRanks} from '../data/kingdom';
import {getCompanionSkillUnlocks} from '../data/companions';
import {capitalize, postDegreeOfSuccessMessage, unslugify} from '../../utils';
import {activityData} from '../data/activityData';
import {DegreeOfSuccess, determineDegreeOfSuccess} from '../../degree-of-success';

export type CheckType = 'skill' | 'activity';

export interface CheckDialogFeatOptions {
    type: CheckType;
    activity?: Activity;
    skill?: Skill;
    game: Game;
    kingdom: Kingdom;
    onRoll: (consumeModifiers: Set<string>) => Promise<void>;
}

interface CheckFormData {
    phase: KingdomPhase | '-';
    dc: number;
    selectedSkill: Skill;
    customModifiers: CustomModifiers;
    overrideModifiers: Record<string, string>;
    consumeModifiers: Record<string, boolean>;
}

interface TotalAndModifiers {
    total: ModifierTotals;
    modifiers: ModifierWithId[];
}

type TotalFields = 'circumstance' | 'untyped' | 'item' | 'status';
type CustomModifiers = Pick<ModifierTotals, TotalFields>;

export class CheckDialog extends FormApplication<FormApplicationOptions & CheckDialogFeatOptions, object, null> {
    private type: CheckType;
    private activity: Activity | undefined;
    private skill: Skill | undefined;
    private game: Game;
    private kingdom: Kingdom;
    private selectedSkill: Skill;
    private dc: number;
    private phase: KingdomPhase | undefined;
    private customModifiers: CustomModifiers = {
        item: {bonus: 0, penalty: 0},
        circumstance: {bonus: 0, penalty: 0},
        status: {bonus: 0, penalty: 0},
        untyped: {bonus: 0, penalty: 0},
    };
    private modifierOverrides: Record<string, boolean> = {};
    private consumeModifiers: Set<string> = new Set();
    private onRoll: (consumeModifiers: Set<string>) => Promise<void>;

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

    constructor(object: null, options: Partial<FormApplicationOptions> & CheckDialogFeatOptions) {
        super(object, options);
        this.type = options.type;
        this.activity = options.activity;
        this.skill = options.skill;
        this.game = options.game;
        this.kingdom = options.kingdom;
        this.onRoll = options.onRoll;
        const controlDC = getControlDC(this.kingdom.level, this.kingdom.size);
        if (this.type === 'skill') {
            this.selectedSkill = options.skill!;
            this.dc = controlDC;
        } else {
            this.phase = getActivityPhase(this.activity!);
            this.selectedSkill = this.getActivitySkills(options.kingdom.skillRanks)[0];
            const activityDCType = activityData[this.activity!].dc;
            if (activityDCType === 'control') {
                this.dc = controlDC;
            } else if (activityDCType === 'custom') {
                this.dc = 0;
            } else if (activityDCType === 'none') {
                throw Error('Can not perform activity with no DC');
            } else {
                this.dc = activityDCType;
            }
        }
    }

    private getActivitySkills(ranks: SkillRanks): Skill[] {
        const activity = this.activity!;
        const companionUnlockSkills = (Object.entries(getCompanionSkillUnlocks(this.kingdom.leaders)) as [Skill, Activity[]][])
            .filter(([, activities]) => activities.includes(activity))
            .map(([skill]) => skill);
        const activitySkills = getActivitySkills(activity, ranks);
        return Array.from(new Set([...activitySkills, ...companionUnlockSkills]));
    }

    override getData(options?: Partial<FormApplicationOptions & { feats: KingdomFeat[] }>): Promise<object> | object {
        const settlementScene = getSettlementScene(this.game, this.kingdom.activeSettlement);
        const activeSettlement = settlementScene ? getMergedData(this.game, settlementScene) : undefined;
        const skillRanks = this.kingdom.skillRanks;
        const applicableSkills = this.type === 'skill' ? [this.skill!] : this.getActivitySkills(skillRanks);
        const additionalModifiers: Modifier[] = createAdditionalModifiers(this.kingdom, activeSettlement);
        const convertedCustomModifiers: Modifier[] = this.createCustomModifiers(this.customModifiers);
        const skillModifiers = Object.fromEntries(applicableSkills.map(skill => {
            const ability = skillAbilities[skill];
            const modifiers = createSkillModifiers({
                ruin: this.kingdom.ruin,
                unrest: this.kingdom.unrest,
                skillRank: skillRanks[skill],
                abilityScores: this.kingdom.abilityScores,
                leaders: this.kingdom.leaders,
                kingdomLevel: this.kingdom.level,
                alwaysAddLevel: getBooleanSetting(this.game, 'kingdomAlwaysAddLevel'),
                ability,
                skillItemBonus: activeSettlement?.settlement?.skillBonuses?.[skill],
                additionalModifiers: [...additionalModifiers, ...convertedCustomModifiers],
                activity: this.activity,
                phase: this.phase,
                skill,
                overrides: this.modifierOverrides,
            });
            const total = calculateModifiers(modifiers);
            return [skill, {total, modifiers}];
        })) as Record<Skill, TotalAndModifiers>;
        // set all modifiers as consumed that have a consumeId and are enabled
        this.consumeModifiers = new Set(skillModifiers[this.selectedSkill].modifiers
            .filter(modifier => modifier.enabled && modifier.consumeId !== undefined)
            .map(modifier => modifier.consumeId!));
        return {
            ...super.getData(options),
            invalid: this.selectedSkill === undefined,
            dc: this.dc,
            title: this.activity ? unslugify(this.activity) : capitalize(this.skill!),
            activity: this.activity,
            selectableSkills: this.createSelectableSkills(skillModifiers),
            selectedSkill: this.selectedSkill,
            modifiers: this.createModifiers(skillModifiers[this.selectedSkill]),
            phase: this.phase,
            customModifiers: this.customModifiers,
        };
    }

    /* eslint-disable @typescript-eslint/no-explicit-any */
    protected async _updateObject(event: Event, formData: any): Promise<void> {
        const data = expandObject(formData) as CheckFormData;
        console.log(data);
        this.selectedSkill = data.selectedSkill;
        this.dc = data.dc;
        this.phase = data.phase === '-' ? undefined : data.phase;
        this.customModifiers = this.homogenize(data.customModifiers);
        this.modifierOverrides = (Object.entries(data.overrideModifiers ?? {}) as [string, string][])
            .filter(([, state]) => state !== '-')
            .map(([id, state]) => {
                return {[id]: state === 'enabled'};
            })
            .reduce((a, b) => Object.assign(a, b), {});
        this.render();
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        const $html = html[0];
        $html.querySelector('#km-roll-skill-assurance')?.addEventListener('click', async (event) => {
            const target = event.currentTarget as HTMLButtonElement;
            const dc = parseInt(target.dataset.dc ?? '0', 10);
            const modifier = parseInt(target.dataset.modifier ?? '0', 10);
            const activity = target.dataset.activity as Activity | undefined;
            const type = target.dataset.type!;

            await this.rollCheck(`${modifier}`, type, activity, dc);
            await this.onRoll(this.consumeModifiers);
            await this.close();
        });

        $html.querySelector('#km-roll-skill')?.addEventListener('click', async (event) => {
            const target = event.currentTarget as HTMLButtonElement;
            const dc = parseInt(target.dataset.dc ?? '0', 10);
            const modifier = parseInt(target.dataset.modifier ?? '0', 10);
            const activity = target.dataset.activity as Activity | undefined;
            const type = target.dataset.type!;

            await this.rollCheck(`1d20+${modifier}`, type, activity, dc);
            await this.onRoll(this.consumeModifiers);
            await this.close();
        });
    }


    private async rollCheck(formula: string, type: string, activity: Activity | undefined, dc: number): Promise<void> {
        const roll = await new Roll(formula).roll();
        await roll.toMessage({flavor: `Rolling Skill Check: ${type}, DC ${dc}`});
        const total = roll.total;
        const degreeOfSuccess = determineDegreeOfSuccess(10, total, dc);
        await this.postDegreeOfSuccess(activity, degreeOfSuccess);
    }

    private async postDegreeOfSuccess(activity: Activity | undefined, degreeOfSuccess: DegreeOfSuccess): Promise<void> {
        if (activity) {
            const results = activityData[activity];
            await postDegreeOfSuccessMessage(degreeOfSuccess, {
                critSuccess: `<b>Critical Success</b>${results.criticalSuccess ? `: ${results.criticalSuccess}` : ''}`,
                success: `<b>Success</b>${results.success ? `: ${results.success}` : ''}`,
                failure: `<b>Failure</b>${results.failure ? `: ${results.failure}` : ''}`,
                critFailure: `<b>Critical Failure</b>${results.criticalFailure ? `: ${results.criticalFailure}` : ''}`,
            });
        } else {
            await postDegreeOfSuccessMessage(degreeOfSuccess, {
                critSuccess: '<b>Critical Success</b>',
                success: '<b>Success</b>',
                failure: '<b>Failure</b>',
                critFailure: '<b>Critical Failure</b>',
            });
        }
    }

    private createSelectableSkills(skillModifiers: Record<Skill, TotalAndModifiers>): object {
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
                    name: 'Custom',
                    enabled: true,
                }, {
                    value: -values.penalty,
                    type,
                    name: 'Custom',
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
