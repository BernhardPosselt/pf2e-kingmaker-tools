import {
    ActionBonuses,
    evaluateStructures,
    getKingdomData,
    getSettlementData,
    ItemLevelBonuses, KingdomData, SkillItemBonuses,
    Structure,
    StructureResult,
} from './structures';
import {structuresByName} from './structure-data';

function getViewedSceneStructures(game: Game): Structure[] {
    const scene = game.scenes?.viewed;
    if (scene) {
        return getSceneStructures(scene);
    } else {
        return [];
    }
}

class StructureError extends Error {
}

function parseStructureData(name: string | null, data: unknown): Structure | undefined {
    if (data === undefined || data === null) {
        return undefined;
    } else if (typeof data === 'object' && 'ref' in data) {
        const refData = data as { ref: string };
        const lookedUpStructure = structuresByName.get(refData.ref);
        if (lookedUpStructure === undefined) {
            throw new StructureError(`No predefined structure data found for actor with name ${name}`);
        }
        return lookedUpStructure;
    } else if (name !== null) {
        return {
            name,
            ...data,
        };
    } else {
        return data as Structure;
    }
}

function getSceneStructures(scene: Scene): Structure[] {
    try {
        return scene.tokens
            .filter(t => t.actor !== null && t.actor !== undefined)
            .map(t => t.actor)
            .map(actor => parseStructureData(actor!.name, actor!.getFlag('pf2e-kingmaker-tools', 'structureData')))
            .filter(data => data !== undefined) as Structure[] ?? [];
    } catch (e: unknown) {
        if (e instanceof StructureError) {
            ui.notifications?.error(e.message);
        } else {
            throw e;
        }
    }
    return [];
}


interface SettlementOptions {
    game: Game;
}

interface SettlementFormData {

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
        options.template = 'modules/pf2e-kingmaker-tools/templates/settlement.html';
        options.submitOnChange = true;
        options.closeOnSubmit = false;
        options.classes = ['kingmaker-tools-app', 'settlement-app'];
        options.width = 500;
        return options;
    }

    private readonly game: Game;

    constructor(object: null, options: Partial<FormApplicationOptions> & SettlementOptions) {
        super(object, options);
        this.game = options.game;
    }

    override getData(options?: Partial<FormApplicationOptions>): object {
        const scene = {
            kingdomSize: 25,
            settlementLevel: 14,
            settlementType: 'Capital',
            name: this.game.scenes?.viewed?.name,
        };
        const kingdom = getKingdomData(scene.kingdomSize);
        const settlement = getSettlementData(scene.settlementLevel);
        const structures = evaluateStructures(getViewedSceneStructures(this.game), settlement.maxItemBonus);
        console.log(structures);
        return {
            ...super.getData(options),
            ...scene,
            ...settlement,
            consumption: Math.max(0, settlement.consumption - structures.consumptionReduction),
            capitalInvestmentPossible: structures.allowCapitalInvestment ? 'yes' : 'no',
            settlementEventBonus: structures.settlementEventBonus,
            notes: structures.notes,
            leadershipActivities: structures.increaseLeadershipActivities ? 3 : 2,
            settlementTypes: ['-', 'Settlement', 'Capital'],
            availableItems: this.getAvailableItems(scene.settlementLevel, structures.itemLevelBonuses),
            storage: this.calculateStorage(structures, kingdom),
            skillItemBonuses: this.getSkillBonuses(structures.skillBonuses),
        };
    }


    override async _updateObject(event: Event, formData?: SettlementFormData): Promise<void> {
        console.log(formData);
        this.render();
    }

    public sceneChange(): void {
        this.render();
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        Hooks.on('canvasReady', this.sceneChange.bind(this));

    }

    override close(options?: FormApplication.CloseOptions): Promise<void> {
        Hooks.off('canvasReady', this.sceneChange);
        return super.close(options);
    }

    private capitalize(word: string): string {
        return word[0].toUpperCase() + word.substring(1);
    }

    private unslugifyAction(word: string): string {
        return word
            .replaceAll('action:', '')
            .split('-')
            .map(part => this.capitalize(part))
            .join(' ');
    }

    private getAvailableItems(settlementLevel: number, itemLevelBonuses: ItemLevelBonuses): LabeledData<number>[] {
        return Object.entries(itemLevelBonuses)
            .map(([type, bonus]) => {
                return {
                    label: this.capitalize(type),
                    value: Math.max(0, settlementLevel + bonus),
                };
            });
    }

    private calculateStorage(structures: StructureResult, kingdom: KingdomData): LabeledData[] {
        return Object.entries(structures.storage)
            .map(([type, bonus]) => {
                return {
                    label: this.capitalize(type),
                    value: bonus + kingdom.commodityStorage,
                };
            });
    }

    private getSkillBonuses(skillBonuses: SkillItemBonuses): SkillBonusData[] {
        return Object.entries(skillBonuses)
            .map(([skill, bonus]) => {
                return {
                    label: this.capitalize(skill),
                    value: bonus.value,
                    actions: (Object.entries(bonus.actions) as ([keyof ActionBonuses, number])[])
                        .map(([action, value]) => {
                            return {
                                label: this.unslugifyAction(action),
                                value: value,
                            };
                        }),
                };
            });
    }
}

export async function showStructureBonuses(game: Game): Promise<void> {
    new SettlementApp(null, {game}).render(true);
}

function editTemplate(structureData: object | undefined): string {
    const root = document.createElement('form');
    const textarea = document.createElement('textarea');
    textarea.name = 'json';
    textarea.innerText = structureData ? JSON.stringify(structureData) : '';
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
                    console.log(value);
                    await actor.setFlag('pf2e-kingmaker-tools', 'structureData', value);
                },
            },
        },
        default: 'roll',
    }, {
        jQuery: false,
    }).render(true, {width: 400, classes: ['edit-structure-json', 'kingmaker-tools-app']});
}
