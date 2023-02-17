import {getMergedData, getSettlementScene} from '../scene';
import {ActivityBonuses, ItemLevelBonuses, SkillItemBonuses} from '../data/structures';
import {capitalize, unslugifyActivity} from '../../utils';
import {SettlementData} from '../structures';

interface SettlementOptions {
    game: Game;
    settlementId: string;
}

interface LabeledData<T = string> {
    label: string;
    value: T;
}

interface SkillBonusData extends LabeledData<number> {
    actions: LabeledData<number>[];
}

class SettlementApp extends Application<ApplicationOptions & SettlementOptions> {
    static override get defaultOptions(): ApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'settlement-app';
        options.title = 'Settlement';
        options.template = 'modules/pf2e-kingmaker-tools/templates/kingdom/settlement.hbs';
        options.classes = ['kingmaker-tools-app', 'settlement-app'];
        options.width = 800;
        options.height = 'auto';
        return options;
    }

    private readonly game: Game;
    private readonly settlementId: string;
    constructor(options: Partial<ApplicationOptions> & SettlementOptions) {
        super(options);
        this.game = options.game;
        this.settlementId = options.settlementId;
    }

    override getData(options?: Partial<ApplicationOptions>): object {
        const scene = getSettlementScene(this.game, this.settlementId)!;
        const data = getMergedData(this.game, scene)!;
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
            availableItems: this.getAvailableItems(settlementLevel, structures.itemLevelBonuses),
            storage,
            showStorage: Object.keys(storage).length > 0,
            skillItemBonuses: this.getSkillBonuses(structures.skillBonuses),
        };
    }
    public sceneChange(): void {
        this.render();
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        Hooks.on('createToken', this.sceneChange.bind(this));
        Hooks.on('deleteToken', this.sceneChange.bind(this));
    }

    override close(options?: FormApplication.CloseOptions): Promise<void> {
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
            .filter(([, bonus]) => bonus.value > 0 || (bonus.activities && Object.keys(bonus.activities).length > 0))
            .map(([skill, bonus]) => {
                return {
                    label: capitalize(skill),
                    value: bonus.value,
                    actions: (Object.entries(bonus.activities) as ([keyof ActivityBonuses, number])[])
                        .map(([action, value]) => {
                            return {
                                label: unslugifyActivity(action),
                                value: value,
                            };
                        }),
                };
            });
    }
}

export async function showSettlement(game: Game, settlementId: string): Promise<void> {
    new SettlementApp({game, settlementId}).render(true);
}
