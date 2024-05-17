import {
    capitalize,
    createUUIDLink,
    deCamelCase,
    escapeHtml,
    isBlank,
    LabelAndValue,
    listenClick,
    parseSelect,
    parseTextInput,
    range,
    unslugify,
} from '../../utils';
import {ActivityEffect, ActivityOutcome, CampingActivityData, CampingActivityName, EffectTarget} from '../activities';
import {getEffectByUuid} from '../actor';
import {DcType, Proficiency} from '../data';
import {skillDialog, SkillView} from '../../common/skills-dialog';
import {allActorSkills, CharacterSkill} from '../../kingdom/data/skills';

export interface AddActivityOptions {
    onSubmit: (activity: CampingActivityData) => Promise<void>;
    homebrewActivities: CampingActivityData[];
    activity?: CampingActivityData;
}

interface Effect {
    target: string;
    effect: string;
    key: string;
}

interface ViewOutcome {
    id: string;
    label: string;
    message: string;
    effects: Effect[];
    modifyRandomEncounterDc: {
        day: number;
        night: number;
    };
    checkRandomEncounter: boolean;
}

interface ViewData {
    outcomes: ViewOutcome[];
    effects: Effect[];
    name: CampingActivityName;
    journalUuid: string;
    dc: DcType | '';
    modifyRandomEncounterDc: {
        day: number;
        night: number;
    },
    isSecret: boolean;
    errors: string[];
    dcs: LabelAndValue[];
    skills: SkillView[];
}

const allOutcomes = ['criticalSuccess', 'success', 'failure', 'criticalFailure'] as const;
type  ActivityOutcomeType = typeof allOutcomes[number];

type FormData = {
    'day-encounter-dc': number;
    'night-encounter-dc': number;
    dc: string;
    journal: string;
    name: string;
    secret: false;
    'skill-proficiency': Proficiency;
    'skill-requirement': string;
    skills: string;
    'criticalFailure-day-modifier': number;
    'criticalFailure-message': string;
    'criticalFailure-night-modifier': number;
    'criticalFailure-roll-encounter': boolean;
    'failure-day-modifier': number;
    'failure-message': string;
    'failure-night-modifier': number;
    'failure-roll-encounter': boolean;
    'criticalSuccess-day-modifier': number;
    'criticalSuccess-message': string;
    'criticalSuccess-night-modifier': number;
    'criticalSuccess-roll-encounter': boolean;
    'success-day-modifier': number;
    'success-message': string;
    'success-night-modifier': number;
    'success-roll-encounter': boolean;
};

function emptyOutcome(): ActivityOutcome {
    return {
        checkRandomEncounter: false,
        effectUuids: [],
        modifyRandomEncounterDc: {
            day: 0,
            night: 0,
        },
        message: '',
    };
}

class AddCampingActivities extends FormApplication<FormApplicationOptions & AddActivityOptions, object, null> {
    private onSubmitCallback: (activity: CampingActivityData) => Promise<void>;
    private homebrewActivities: CampingActivityData[];
    private activity: CampingActivityData;
    private originalName: CampingActivityName | undefined;

    static override get defaultOptions(): FormApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'kingdom-add-camping-activities';
        options.title = 'Manage Camping Activities';
        options.template = 'modules/pf2e-kingmaker-tools/templates/camping/add-activities.hbs';
        options.submitOnChange = true;
        options.closeOnSubmit = false;
        options.classes = [];
        options.height = 'auto';
        return options;
    }

    constructor(object: null, options: Partial<FormApplicationOptions> & AddActivityOptions) {
        super(object, options);
        this.onSubmitCallback = options.onSubmit;
        this.homebrewActivities = foundry.utils.deepClone(options.homebrewActivities);
        this.originalName = options.activity?.name;
        this.activity = foundry.utils.deepClone(options.activity) ?? {
            journalUuid: '',
            isHomebrew: true,
            dc: 'zone',
            effectUuids: [],
            modifyRandomEncounterDc: {
                night: 0,
                day: 0,
            },
            isLocked: false,
            skills: 'any',
            isSecret: false,
            name: '' as CampingActivityName,
            skillRequirements: [],
            criticalFailure: emptyOutcome(),
            criticalSuccess: emptyOutcome(),
            success: emptyOutcome(),
            failure: emptyOutcome(),
        };
    }

    override async getData(): Promise<ViewData> {
        return {
            outcomes: await Promise.all(allOutcomes.map(async (s) => {
                const activity = this.activity[s];
                return {
                    id: s,
                    label: unslugify(s),
                    checkRandomEncounter: activity?.checkRandomEncounter ?? false,
                    message: activity?.message ?? '',
                    modifyRandomEncounterDc: {
                        day: activity?.modifyRandomEncounterDc?.day ?? 0,
                        night: activity?.modifyRandomEncounterDc?.night ?? 0,
                    },
                    effects: await this.getEffects(activity?.effectUuids ?? [], s),
                };
            })),
            effects: await this.getEffects(this.activity.effectUuids ?? [], 'effect'),
            dc: this.activity.dc ?? '',
            isSecret: this.activity.isSecret ?? false,
            journalUuid: this.activity.journalUuid ?? '',
            modifyRandomEncounterDc: {
                day: this.activity.modifyRandomEncounterDc?.day ?? 0,
                night: this.activity.modifyRandomEncounterDc?.night ?? 0,
            },
            name: this.activity.name ?? '',
            errors: await this.validate(this.activity),
            dcs: ['actorLevel', 'zone', ...range(0, 60)].map(e => {
                return {label: deCamelCase(e.toString()), value: e.toString()};
            }),
            skills: this.activity.skills === 'any' ? [{id: 'all', label: 'All'}] : this.activity.skills.map(s => {
                const proficiency = this.activity.skillRequirements
                    .find(r => r.skill === s)?.proficiency;
                return {
                    label: unslugify(s),
                    id: s,
                    proficiency,
                };
            }),
        };
    }

    private async getEffects(effectUuids: ActivityEffect[], key: string): Promise<Effect[]> {
        return await Promise.all(effectUuids?.map(async (e): Promise<Effect> => {
            return {
                key,
                target: e.target ? capitalize(e.target) : 'All',
                effect: await TextEditor.enrichHTML(createUUIDLink(e.uuid)),
            };
        }));
    }

    protected async _updateObject(event: Event, formData: FormData): Promise<void> {
        console.log(formData);
        this.activity.journalUuid = formData.journal;
        this.activity.name = formData.name as CampingActivityName;
        this.activity.isSecret = formData.secret;
        this.activity.isLocked = false;
        this.activity.dc = parseDC(formData.dc);
        if (formData['night-encounter-dc'] || formData['day-encounter-dc']) {
            this.activity.modifyRandomEncounterDc = {
                day: formData['day-encounter-dc'] ?? 0,
                night: formData['night-encounter-dc'] ?? 0,
            };
        } else {
            this.activity.modifyRandomEncounterDc = undefined;
        }
        this.activity.isHomebrew = true;
        allOutcomes.forEach(outcome => {
            this.activity[outcome]!.message = formData[`${outcome}-message`];
            this.activity[outcome]!.checkRandomEncounter = formData[`${outcome}-roll-encounter`];
            this.activity[outcome]!.modifyRandomEncounterDc!.day = formData[`${outcome}-day-modifier`];
            this.activity[outcome]!.modifyRandomEncounterDc!.night = formData[`${outcome}-night-modifier`];
        });
        this.render();
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        const $html = html[0];
        listenClick($html, '.save', async (): Promise<void> => {
            await this.onSubmitCallback(this.activity);
            await this.close();
        });
        listenClick($html, '.add-effect', async (ev): Promise<void> => {
            const button = ev.currentTarget as HTMLButtonElement;
            const type = button.dataset.type as ActivityOutcomeType | 'effect';
            const value: ActivityEffect = {
                target: 'all',
                uuid: '',
            };
            editActivityDialog(value, () => {
                if (type === 'effect') {
                    this.activity.effectUuids = (this.activity.effectUuids ?? []);
                    this.activity.effectUuids.push(value);
                } else {
                    this.activity[type]!.effectUuids = (this.activity[type]?.effectUuids ?? []);
                    this.activity[type]!.effectUuids!.push(value);
                }
                this.render();
            });
        });
        listenClick($html, '.edit-skills', async (): Promise<void> => {
            const skills = this.activity.skills;
            const selectedLores = skills === 'any' ? [] : skills
                .filter(s => !allActorSkills.includes(s as CharacterSkill))
                .map(s => {
                    return {
                        id: s,
                        proficiency: this.activity.skillRequirements.find(req => req.skill === s)?.proficiency,
                    };
                });
            const selectedSkills = skills === 'any' ? [] : skills
                .filter(s => allActorSkills.includes(s as CharacterSkill))
                .map(s => {
                    return {
                        id: s,
                        proficiency: this.activity.skillRequirements.find(req => req.skill === s)?.proficiency,
                    };
                });
            skillDialog({
                selectedLores,
                selectedSkills,
                atLeastOne: true,
                all: skills === 'any',
                onSave: (data) => {
                    if (data.all) {
                        this.activity.skills = 'any';
                        this.activity.skillRequirements = [];
                    } else {
                        const loresAndSkills = [
                            ...data.selectedSkills,
                            ...data.selectedLores,
                        ];
                        this.activity.skills = loresAndSkills.map(s => s.id);
                        this.activity.skillRequirements = loresAndSkills
                            .filter(s => s.proficiency !== undefined)
                            .map(s => {
                                return {
                                    skill: s.id,
                                    proficiency: s.proficiency!,
                                };
                            });
                    }
                    this.render();
                },
                availableSkills: [...allActorSkills],
            });
        });
        listenClick($html, '.edit-effect', async (ev): Promise<void> => {
            const button = ev.currentTarget as HTMLButtonElement;
            const type = button.dataset.type as ActivityOutcomeType | 'effect';
            const index = parseInt(button.dataset.id ?? '0', 10);
            let value: ActivityEffect;
            if (type === 'effect') {
                value = this.activity.effectUuids![index];
            } else {
                value = this.activity[type]!.effectUuids![index];
            }
            editActivityDialog(value, () => this.render());
        });
        listenClick($html, '.delete-effect', async (ev): Promise<void> => {
            const button = ev.currentTarget as HTMLButtonElement;
            const type = button.dataset.type as ActivityOutcomeType | 'effect';
            const index = parseInt(button.dataset.id ?? '0', 10);
            if (type === 'effect') {
                this.activity.effectUuids = this.activity.effectUuids
                    ?.filter((_, i) => i !== index);
            } else {
                this.activity[type]!.effectUuids = this.activity[type]?.effectUuids
                    ?.filter((_, i) => i !== index);
            }
            this.render();
        });
    }

    private async validate(activity: CampingActivityData): Promise<string[]> {
        const result = [];
        if (isBlank(activity.name)) {
            result.push('Name must not be blank');
        }
        if (this.homebrewActivities.find(a => a.name === activity.name && a.name !== this.originalName)) {
            result.push(`Homebrew Activity with name ${activity.name} already exists`);
        }
        if (activity.journalUuid && !(await fromUuid(activity.journalUuid))) {
            result.push(`Journal with uuid ${activity.journalUuid} does not exist`);
        }
        return result;
    }
}

function editActivityDialog(data: ActivityEffect, onOk: () => void = (): void => {
}): void {
    new Dialog({
        title: 'Add/Edit Effect',
        content: `
        <form class="simple-dialog-form">
           <div>
                <label for="km-effect">Effect UUID</label>
                <input type="text" name="effect" id="km-effect" value="${escapeHtml(data.uuid)}">
            </div>
            <div>
                <label for="km-effect-target">Effect Target</label>
                <select name="effect-target" id="km-effect-target">
                    <option value="all" ${data.target === 'all' ? 'selected' : ''}>All</option>
                    <option value="allies" ${data.target === 'allies' ? 'selected' : ''}>Allies</option>
                    <option value="self" ${data.target === 'self' ? 'selected' : ''}>Self</option>
                </select>
            </div>
        </form>
        `,
        buttons: {
            save: {
                icon: '<i class="fa-solid fa-save"></i>',
                label: 'Save',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const effectUuid = parseTextInput($html, 'effect');
                    const effectTarget = parseSelect($html, 'effect-target') as EffectTarget;
                    const item = await getEffectByUuid(effectUuid);
                    if (item === null) {
                        ui.notifications?.error(`Can not find effect item with uuid ${effectUuid}`);
                    } else {
                        data.uuid = effectUuid;
                        data.target = effectTarget;
                        onOk();
                    }
                },
            },
        },
        default: 'save',
    }, {
        jQuery: false,
        width: 321,
    }).render(true);
}

export function addActivityDialog(options: AddActivityOptions): void {
    new AddCampingActivities(null, options).render(true);
}

function parseDC(value: string): DcType | undefined {
    if (!isBlank(value)) {
        if (value === 'zone') {
            return 'zone';
        } else if (value === 'actorLevel') {
            return 'actorLevel';
        } else {
            const number = parseInt(value, 10);
            if (!isNaN(number)) {
                return number;
            }
        }
    }
    return undefined;
}
