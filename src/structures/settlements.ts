import {
    ActionBonuses,
    ItemLevelBonuses, SkillItemBonuses,
    SettlementData,
} from './structures';
import {getMergedData, saveViewedSceneData} from './scene';
import {capitalize, unslugifyAction} from '../utils';

interface SettlementOptions {
    game: Game;
}

interface SettlementFormData {
    settlementType: string;
    settlementLevel: number;
    overcrowded: boolean;
    secondaryTerritory: boolean;
}

interface LabeledData<T = string> {
    label: string;
    value: T;
}

interface SkillBonusData extends LabeledData<number> {
    actions: LabeledData<number>[];
}

class SettlementApp extends FormApplication<FormApplicationOptions & SettlementOptions, object, null> {
    static override get defaultOptions(): FormApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'settlement-app';
        options.title = 'Settlement';
        options.template = 'modules/pf2e-kingmaker-tools/templates/settlement.hbs';
        options.submitOnChange = true;
        options.closeOnSubmit = false;
        options.classes = ['kingmaker-tools-app', 'settlement-app'];
        options.width = 500;
        options.height = 'auto';
        return options;
    }

    private readonly game: Game;
    constructor(object: null, options: Partial<FormApplicationOptions> & SettlementOptions) {
        super(object, options);
        this.game = options.game;
    }

    override getData(options?: Partial<FormApplicationOptions>): object {
        const isGM = this.game.user?.isGM ?? false;
        const isUser = !isGM;
        const data = getMergedData(this.game)!;
        const structures = data.settlement;
        const sceneData = data.scenedData;
        const settlementLevel = sceneData.settlementLevel || 1;
        const storage = this.getStorage(structures);
        return {
            ...super.getData(options),
            ...structures.config,
            ...sceneData,
            consumption: structures.consumption,
            capitalInvestmentPossible: structures.allowCapitalInvestment ? 'yes' : 'no',
            settlementEventBonus: structures.settlementEventBonus,
            leadershipActivityBonus: structures.leadershipActivityBonus,
            notes: structures.notes,
            showNotes: structures.notes.length > 0,
            leadershipActivities: structures.increaseLeadershipActivities ? 3 : 2,
            settlementTypes: ['-', 'Settlement', 'Capital'],
            availableItems: this.getAvailableItems(settlementLevel, structures.itemLevelBonuses),
            storage,
            showStorage: Object.keys(storage).length > 0,
            skillItemBonuses: this.getSkillBonuses(structures.skillBonuses),
            isGM,
            isUser,
            isSettlement: sceneData.settlementType === 'Capital' || sceneData.settlementType === 'Settlement',
        };
    }

    override async _updateObject(event: Event, formData: SettlementFormData): Promise<void> {
        await saveViewedSceneData(this.game, {
            settlementLevel: formData.settlementLevel,
            settlementType: formData.settlementType,
            overcrowded: formData.overcrowded,
            secondaryTerritory: formData.secondaryTerritory,
        });
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
    }

    override close(options?: FormApplication.CloseOptions): Promise<void> {
        Hooks.off('canvasReady', this.sceneChange);
        Hooks.off('createToken', this.sceneChange);
        Hooks.off('deleteToken', this.sceneChange);
        return super.close(options);
    }

    private getAvailableItems(settlementLevel: number, itemLevelBonuses: ItemLevelBonuses): LabeledData<number>[] {
        const magicTraits = new Set(['arcane', 'divine', 'primal', 'occult']);
        const otherBonus = itemLevelBonuses.other;
        const magicalBonus = itemLevelBonuses.magical;
        return Object.entries(itemLevelBonuses)
            .filter(([type, bonus]) => {
                if (magicTraits.has(type)) {
                    return bonus > otherBonus && bonus > magicalBonus;
                } else if (type !== 'other') {
                    return bonus > otherBonus;
                } else {
                    return true;
                }
            })
            .map(([type, bonus]) => {
                return {
                    label: capitalize(type),
                    value: Math.max(0, settlementLevel + bonus),
                };
            });
    }

    private getStorage(structures: SettlementData): LabeledData[] {
        return Object.entries(structures.storage)
            .filter(([, bonus]) => bonus > 0)
            .map(([type, bonus]) => {
                return {
                    label: capitalize(type),
                    value: bonus,
                };
            });
    }

    private getSkillBonuses(skillBonuses: SkillItemBonuses): SkillBonusData[] {
        return Object.entries(skillBonuses)
            .filter(([, bonus]) => bonus.value > 0 || (bonus.actions && Object.keys(bonus.actions).length > 0))
            .map(([skill, bonus]) => {
                return {
                    label: capitalize(skill),
                    value: bonus.value,
                    actions: (Object.entries(bonus.actions) as ([keyof ActionBonuses, number])[])
                        .map(([action, value]) => {
                            return {
                                label: unslugifyAction(action),
                                value: value,
                            };
                        }),
                };
            });
    }
}

export async function showSettlement(game: Game): Promise<void> {
    new SettlementApp(null, {game}).render(true);
}

function editTemplate(structureData: object | undefined): string {
    const root = document.createElement('form');
    const textarea = document.createElement('textarea');
    textarea.name = 'json';
    textarea.innerHTML = structureData ? JSON.stringify(structureData, null, 2) : '';
    root.appendChild(textarea);
    return root.outerHTML;
}

export async function showStructureEditDialog(game: Game, actor: Actor): Promise<void> {
    const structureData = actor!.getFlag('pf2e-kingmaker-tools', 'structureData') ?? undefined;
    console.log(structureData);
    new Dialog({
        title: 'Edit Structure Data',
        content: editTemplate(structureData),
        buttons: {
            roll: {
                icon: '<i class="fa-solid fa-save"></i>',
                label: 'Save',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const json = $html.querySelector('textarea[name=json]') as HTMLInputElement;
                    const value = json.value.trim() === '' ? null : JSON.parse(json.value);
                    await actor.unsetFlag('pf2e-kingmaker-tools', 'structureData');
                    await actor.setFlag('pf2e-kingmaker-tools', 'structureData', value);
                },
            },
        },
        default: 'roll',
    }, {
        jQuery: false,
    }).render(true, {width: 400, classes: ['edit-structure-json', 'kingmaker-tools-app']});
}
