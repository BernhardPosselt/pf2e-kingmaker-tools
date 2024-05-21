import {ActivityResult, KingdomActivity} from '../data/activityData';
import {blankToUndefined, capitalize, deCamelCase, isBlank, LabelAndValue, listenClick, range} from '../../utils';
import {KingdomPhase} from '../data/activities';
import {allSkills} from '../data/skills';
import {skillDialog, SkillView} from '../../common/skills-dialog';
import {proficiencyToRank, rankToProficiency} from '../modifiers';


interface AddActivityData {
    id: string;
    edit: boolean;
    title: string;
    description: string;
    hint: string;
    special: string;
    requirement: string;
    phase: KingdomPhase;
    phases: LabelAndValue[];
    skills: SkillView[];
    dc: 'control' | 'custom' | 'none' | 'scouting' | number;
    dcs: LabelAndValue[];
    dcAdjustment: number;
    oncePerRound: boolean;
    fortune: boolean;
    criticalSuccess: string;
    success: string;
    failure: string;
    criticalFailure: string;
    errors: string[];
}

interface ActivityFormData {
    id: string;
    title: string;
    description: string;
    hint: string;
    special: string;
    requirement: string;
    phase: KingdomPhase;
    dc: 'control' | 'custom' | 'none' | string;
    dcAdjustment: number;
    oncePerRound: boolean;
    fortune: boolean;
    criticalSuccess: string;
    success: string;
    failure: string;
    criticalFailure: string;
}

interface AddKingdomActivityOptions {
    onOk: (activity: KingdomActivity) => Promise<void>,
    activity?: KingdomActivity,
    homebrewActivities: KingdomActivity[];
}

function parseDc(value: string): 'control' | 'custom' | 'none' | 'scouting' | number {
    if (value === 'control' || value === 'custom' || value === 'none' || value === 'scouting') {
        return value;
    } else {
        return parseInt(value, 10) || 0;
    }
}


function parseOutcome(value: string): ActivityResult | undefined {
    if (isBlank(value)) {
        return undefined;
    } else {
        return {
            msg: value,
        };
    }
}


class AddKingdomActivities extends FormApplication<FormApplicationOptions & AddKingdomActivityOptions, object, null> {
    private onSubmitCallback: (activity: KingdomActivity) => Promise<void>;
    private activity: KingdomActivity;
    private edit: boolean;
    private homebrewActivities: KingdomActivity[];

    static override get defaultOptions(): FormApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'kingdom-add-kingdom-activities';
        options.title = 'Manage Kingdom Activities';
        options.template = 'modules/pf2e-kingmaker-tools/templates/kingdom/add-activity-dialog.hbs';
        options.submitOnChange = true;
        options.closeOnSubmit = false;
        options.classes = [];
        options.height = 'auto';
        options.width = 400;
        return options;
    }

    constructor(object: null, options: Partial<FormApplicationOptions> & AddKingdomActivityOptions) {
        super(object, options);
        this.onSubmitCallback = options.onOk;
        this.edit = options.activity !== undefined;
        this.activity = {
            special: options.activity?.special ?? '',
            requirement: options.activity?.requirement ?? '',
            description: options.activity?.description ?? '',
            dcAdjustment: options.activity?.dcAdjustment ?? 0,
            fortune: options.activity?.fortune ?? false,
            failure: {msg: options.activity?.failure?.msg ?? ''},
            success: {msg: options.activity?.success?.msg ?? ''},
            criticalFailure: {msg: options.activity?.criticalFailure?.msg ?? ''},
            criticalSuccess: {msg: options.activity?.criticalSuccess?.msg ?? ''},
            dc: options.activity?.dc ?? 'control',
            title: options.activity?.title ?? '',
            hint: options.activity?.hint ?? '',
            id: options.activity?.id ?? '',
            skills: options.activity?.skills ?? {agriculture: 0},
            phase: options.activity?.phase ?? 'leadership',
            oncePerRound: options.activity?.oncePerRound ?? false,
            enabled: options.activity?.enabled ?? true,
        };
        this.homebrewActivities = options.homebrewActivities;
    }

    override async getData(): Promise<AddActivityData> {
        return {
            phases: ['army', 'leadership', 'region'].map(p => {
                return {
                    label: capitalize(p),
                    value: p,
                };
            }),
            phase: this.activity.phase,
            dc: this.activity.dc,
            fortune: this.activity.fortune,
            edit: this.edit,
            hint: this.activity.hint ?? '',
            id: this.activity.id,
            title: this.activity.title,
            special: this.activity.special ?? '',
            requirement: this.activity.requirement ?? '',
            description: this.activity.description,
            oncePerRound: this.activity.oncePerRound,
            dcAdjustment: this.activity.dcAdjustment ?? 0,
            criticalSuccess: this.activity.criticalSuccess?.msg ?? '',
            success: this.activity.success?.msg ?? '',
            failure: this.activity.failure?.msg ?? '',
            criticalFailure: this.activity.criticalFailure?.msg ?? '',
            dcs: ['none', 'control', 'custom', 'scouting', ...range(14, 51).map(i => i.toString())]
                .map(v => {
                    return {
                        value: v,
                        label: deCamelCase(v),
                    };
                }),
            skills: Array.from(Object.entries(this.activity.skills))
                .map(([key, rank]) => {
                    return {
                        proficiency: rankToProficiency(rank),
                        id: key,
                        label: capitalize(key),
                    };
                }),
            errors: await this.validate(this.activity),
        };
    }

    protected async _updateObject(event: Event, formData: ActivityFormData): Promise<void> {
        console.log(formData);
        if (!this.edit) {
            this.activity.id = formData.id;
        }
        this.activity.title = formData.title;
        this.activity.description = formData.description;
        this.activity.hint = blankToUndefined(formData.hint);
        this.activity.special = blankToUndefined(formData.special);
        this.activity.requirement = blankToUndefined(formData.requirement);
        this.activity.phase = formData.phase;
        this.activity.dc = parseDc(formData.dc);
        this.activity.dcAdjustment = formData.dcAdjustment;
        this.activity.oncePerRound = formData.oncePerRound;
        this.activity.fortune = formData.fortune;
        this.activity.criticalSuccess = parseOutcome(formData.criticalSuccess);
        this.activity.success = parseOutcome(formData.success);
        this.activity.failure = parseOutcome(formData.failure);
        this.activity.criticalFailure = parseOutcome(formData.criticalFailure);
        this.render();
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        const $html = html[0];
        listenClick($html, '.save', async (): Promise<void> => {
            await this.onSubmitCallback(this.activity);
            await this.close();
        });
        listenClick($html, '.edit-skills', async (): Promise<void> => {
            const skills = this.activity.skills;
            const selectedSkills = Object.entries(skills)
                .map(([s, rank]) => {
                    return {
                        id: s,
                        proficiency: rankToProficiency(rank),
                    };
                }) || undefined;
            skillDialog({
                selectedSkills,
                atLeastOne: true,
                onSave: (data) => {
                    this.activity.skills = Object.fromEntries(data.selectedSkills.map(skill => {
                        return [skill.id, proficiencyToRank(skill.proficiency)];
                    }));
                    this.render();
                },
                availableSkills: [...allSkills],
            });
        });
    }

    private async validate(activity: KingdomActivity): Promise<string[]> {
        const result = [];
        if (!/^[a-z\\-]+$/.test(activity.id)) {
            result.push('ID must only contain lower case English letters and hyphens!');
        }
        if (this.homebrewActivities.find(a => activity.id === a.id) && !this.edit) {
            result.push(`Homebrew activity with id ${activity.id} exists already, can not add another one`);
        }
        if (isBlank(activity.title)) {
            result.push('Title must not be blank');
        }
        return result;
    }
}

export function addActivityDialog(
    options: AddKingdomActivityOptions,
): void {
    new AddKingdomActivities(null, options).render(true);
}