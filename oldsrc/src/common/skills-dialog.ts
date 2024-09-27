import {
    distinctBy,
    isBlank,
    LabelAndValue,
    listenClick,
    loreToLoreSkill,
    slugifyable,
    toLabelAndValue,
    unslugify,
} from '../utils';
import {allTrainedSkillRanks} from '../kingdom/data/skills';
import {Proficiency} from "../kingdom/modifiers";


interface Skill {
    id: string;
    proficiency?: Proficiency;
}

export interface SaveSkillData {
    selectedSkills: Skill[];
    selectedLores: Skill[];
    all: boolean;
    allProficiency?: Proficiency;
}

interface SkillDialogOptions {
    availableSkills: string[];
    selectedSkills?: Skill[];
    selectedLores?: Skill[];
    all?: boolean;
    atLeastOne?: boolean;
    allProficiency?: Proficiency;
    onSave: (data: SaveSkillData) => void;
}

type SkillsFormData = Record<string, unknown>

interface SelectableSkillView extends SkillView {
    selected: boolean;
}

export interface SkillView {
    id: string;
    label: string;
    proficiency?: Proficiency;
}

interface SkillsView {
    showLores: boolean;
    lores?: SkillView[];
    skills: SelectableSkillView[];
    proficiencies: LabelAndValue[];
    all: boolean;
    showAll: boolean;
    allProficiency?: Proficiency;
    errors: string[];
}

class SkillDialog extends FormApplication<FormApplicationOptions & SkillDialogOptions, object, null> {
    private onOk: (data: SaveSkillData) => void;
    private skills: string[];
    private selectedSkills: Skill[];
    private selectedLores: Skill[];
    private showLores: boolean;
    private all: boolean;
    private allProficiency?: Proficiency;
    private showAll: boolean;
    private atLeastOne: boolean;

    static override get defaultOptions(): FormApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'kingdom-skills-dialog';
        options.title = 'Choose Skills';
        options.template = 'modules/pf2e-kingmaker-tools/templates/common/skills-dialog.hbs';
        options.submitOnChange = true;
        options.closeOnSubmit = false;
        options.classes = [];
        options.height = 'auto';
        options.width = 250;
        return options;
    }

    constructor(object: null, options: Partial<FormApplicationOptions> & SkillDialogOptions) {
        super(object, options);
        this.skills = options.availableSkills;
        this.selectedSkills = options.selectedSkills ?? [];
        this.selectedLores = options.selectedLores ?? [];
        this.showLores = options.selectedLores !== undefined;
        this.onOk = options.onSave;
        this.all = options.all ?? false;
        this.showAll = options.all !== undefined;
        this.allProficiency = options.allProficiency;
        this.atLeastOne = options.atLeastOne ?? false;
    }

    override getData(): Promise<SkillsView> | SkillsView {
        return {
            skills: this.skills.map(skill => {
                const selected = this.selectedSkills.find(s => s.id === skill);
                return {
                    id: skill,
                    label: unslugify(skill),
                    selected: selected !== undefined,
                    proficiency: selected?.proficiency,
                };
            }),
            lores: [...this.selectedLores.map(lore => {
                return {
                    id: lore.id,
                    label: unslugify(lore.id),
                    proficiency: lore?.proficiency,
                };
            }), {id: '', label: ''}],
            showLores: this.showLores,
            proficiencies: toLabelAndValue([...allTrainedSkillRanks], {capitalizeLabel: true}), showAll: this.showAll,
            all: this.all,
            allProficiency: this.allProficiency,
            errors: this.validate(),
        };
    }

    protected async _updateObject(event: Event, formData: SkillsFormData): Promise<void> {
        console.log(formData);
        this.selectedSkills = this.skills
            .filter(skill => formData[skill])
            .map(skill => {
                const proficiency = formData[`${skill}-proficiency`];
                return {
                    id: skill,
                    proficiency: proficiency === '-' ? undefined : (proficiency as Proficiency),
                };
            });
        const lores = Object.entries(formData)
            .filter(([id, label]) => id.endsWith('-lore') && !isBlank(label as string) && slugifyable((label as string).trim()))
            .map(([, label]) => {
                const canonicalId = loreToLoreSkill(label as string);
                const proficiency = formData[canonicalId + '-lore-proficiency'] as Proficiency | undefined;
                return {
                    id: canonicalId,
                    proficiency,
                };
            });
        this.selectedLores = distinctBy(lores, (l) => l.id);
        this.all = formData['all'] as boolean;
        this.allProficiency = formData['all-proficiency'] as Proficiency | undefined;
        this.render();
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        const $html = html[0];
        listenClick($html, '.save', async (): Promise<void> => {
            this.onOk({
                selectedSkills: this.selectedSkills,
                selectedLores: this.selectedLores,
                all: this.all,
                allProficiency: this.allProficiency,
            });
            await this.close();
        });
    }

    private validate(): string[] {
        const result = [];
        if (this.atLeastOne && this.selectedSkills.length === 0 && this.selectedLores.length === 0 && !this.all) {
            result.push('Must select at least one skill');
        }
        return result;
    }
}


export function skillDialog(options: SkillDialogOptions): void {
    new SkillDialog(null, options).render(true);
}