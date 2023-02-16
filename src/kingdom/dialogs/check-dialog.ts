import {allFeats, allFeatsByName, KingdomFeat} from '../data/feats';
import {calculateModifiers, createAdditionalModifiers, Modifier} from '../modifiers';
import {Activity, getActivityPhase, getActivitySkills, skillAbilities} from '../data/activities';
import {Skill} from '../data/skills';
import {createSkillModifiers} from '../skills';
import {getBooleanSetting} from '../../settings';
import {getMergedData} from '../../structures/scene';
import {Kingdom, SkillRanks} from '../data/kingdom';
import {getCompanionSkillUnlocks} from '../data/companions';
import {unslugifyActivity} from '../../utils';


export type CheckType = 'skill' | 'activity';

export interface CheckDialogFeatOptions {
    type: CheckType;
    activeSettlementSceneId?: string;
    activity?: Activity;
    skill?: Skill;
    game: Game;
    kingdom: Kingdom;
}

interface CheckFormData {

}

export class CheckDialog extends FormApplication<FormApplicationOptions & CheckDialogFeatOptions, object, null> {
    private type: CheckType;
    private activeSettlementSceneId: string | undefined;
    private activity: Activity | undefined;
    private skill: Skill | undefined;
    private game: Game;
    private kingdom: Kingdom;
    private selectedSkill: Skill;

    static override get defaultOptions(): FormApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'kingdom-check';
        options.title = 'Kingdom';
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
        this.activeSettlementSceneId = options.activeSettlementSceneId;
        this.activity = options.activity;
        this.skill = options.skill;
        this.game = options.game;
        this.kingdom = options.kingdom;
        if (this.type === 'skill') {
            this.selectedSkill = options.skill!;
        } else {
            this.selectedSkill = this.getActivitySkills(options.kingdom.skillRanks)[0];
        }
    }

    private getActivitySkills(ranks: SkillRanks): Skill[] {
        const activity = this.activity!;
        const companionUnlockSkills = (Object.entries(getCompanionSkillUnlocks(this.kingdom.leaders)) as [Skill, Activity[]][])
            .filter(([, activities]) => activities.includes(activity))
            .map(([skill]) => skill);
        return Array.from(new Set([...getActivitySkills(activity, ranks), ...companionUnlockSkills]));
    }

    override getData(options?: Partial<FormApplicationOptions & { feats: KingdomFeat[] }>): Promise<object> | object {
        const settlementScene = this.game?.scenes?.get(this.kingdom.activeSettlement);
        const activeSettlement = settlementScene ? getMergedData(this.game, settlementScene) : undefined;
        const skillRanks = this.kingdom.skillRanks;
        const applicableSkills = this.type === 'skill' ? [this.skill!] : this.getActivitySkills(skillRanks);
        const phase = this.type === 'skill' ? undefined : getActivityPhase(this.activity!);
        const additionalModifiers: Modifier[] = createAdditionalModifiers(this.kingdom, activeSettlement);
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
                additionalModifiers,
                activity: this.activity,
                phase,
            });
            const total = calculateModifiers(modifiers);
            return [skill, {total, modifiers}];
        }));
        return {
            activity: this.activity ? unslugifyActivity(this.activity) : undefined,
            selectableSkills: Object.values(skillModifiers),
            selectedSkill: this.selectedSkill,
            selectedModifiers: skillModifiers[this.selectedSkill],
            ...super.getData(options),
        };
    }

    protected async _updateObject(event: Event, formData: CheckFormData): Promise<void> {
        console.log(formData);
        this.render();
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        const $html = html[0];
        // $html.querySelector('#roll')?.addEventListener('click', async () => {
        //     console.log('Rolling Check');
        //     await this.close();
        // });
    }


}
