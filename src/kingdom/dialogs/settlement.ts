import {ActivityBonuses, ItemLevelBonuses, SkillItemBonuses} from '../data/structures';
import {capitalize, unslugify} from '../../utils';
import {StructureResult} from '../structures';
import {hasFeat, Kingdom} from '../data/kingdom';
import {getCapitalSettlement, getSettlement, getStructureResult, getStructureStackMode} from '../scene';

interface SettlementOptions {
    game: Game;
    settlementId: string;
    kingdom: Kingdom;
}

interface LabeledData<T = string> {
    label: string;
    value: T;
}

interface SkillBonusData extends LabeledData<number> {
    actions: LabeledData<number>[];
}

interface ItemLevelBonusData {
    label: string;
    value: number;
}

class SettlementApp extends Application<ApplicationOptions & SettlementOptions> {
    private kingdom: Kingdom;

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
        this.kingdom = options.kingdom;
    }

    override getData(options?: Partial<ApplicationOptions>): object {
        const settlement = getSettlement(this.game, this.kingdom, this.settlementId)!;
        const capital = getCapitalSettlement(this.game, this.kingdom);
        const structureStackMode = getStructureStackMode(this.game);
        const structures = getStructureResult(structureStackMode, settlement, capital);
        const storage = this.getStorage(structures);
        return {
            ...super.getData(options),
            name: settlement.scene.name,
            type: capitalize(settlement.settlement.type),
            level: settlement.settlement.level,
            secondaryTerritory: settlement.settlement.secondaryTerritory,
            hasBridge: structures.hasBridge,
            lots: settlement.settlement.lots,
            config: structures.config,
            overcrowded: settlement.settlement.lots > structures.residentialLots,
            residentialLots: structures.residentialLots,
            consumption: structures.consumption,
            capitalInvestmentPossible: structures.allowCapitalInvestment ? 'yes' : 'no',
            settlementEventBonus: structures.settlementEventBonus,
            leadershipActivityBonus: structures.leadershipActivityBonus,
            notes: structures.notes,
            showNotes: structures.notes.length > 0,
            leadershipActivities: structures.increaseLeadershipActivities ? 3 : 2,
            availableItems: this.getAvailableItems(settlement.settlement.level, structures.itemLevelBonuses),
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

    private getAvailableItems(settlementLevel: number, itemLevelBonuses: ItemLevelBonuses): ItemLevelBonusData[] {
        const qualityOfLifeBonus = hasFeat(this.kingdom, 'Quality of Life') ? 1 : 0;
        const bonuses: ItemLevelBonuses = {
            alchemical: itemLevelBonuses.alchemical + settlementLevel,
            magical: itemLevelBonuses.magical + qualityOfLifeBonus + settlementLevel,
            luxuryMagical: itemLevelBonuses.luxuryMagical + qualityOfLifeBonus + settlementLevel,
            arcane: itemLevelBonuses.arcane + qualityOfLifeBonus + settlementLevel,
            luxuryArcane: itemLevelBonuses.luxuryArcane + qualityOfLifeBonus + settlementLevel,
            divine: itemLevelBonuses.divine + qualityOfLifeBonus + settlementLevel,
            luxuryDivine: itemLevelBonuses.luxuryDivine + qualityOfLifeBonus + settlementLevel,
            occult: itemLevelBonuses.occult + qualityOfLifeBonus + settlementLevel,
            luxuryOccult: itemLevelBonuses.luxuryOccult + qualityOfLifeBonus + settlementLevel,
            primal: itemLevelBonuses.primal + qualityOfLifeBonus + settlementLevel,
            luxuryPrimal: itemLevelBonuses.luxuryPrimal + qualityOfLifeBonus + settlementLevel,
            other: itemLevelBonuses.other + settlementLevel,
        };
        return [
            {label: 'Alchemical', value: bonuses.alchemical},
            {label: 'Magical', value: bonuses.magical},
            {label: 'Magical (Luxury)', value: bonuses.luxuryMagical},
            {label: 'Arcane', value: bonuses.arcane},
            {label: 'Arcane (Luxury)', value: bonuses.luxuryArcane},
            {label: 'Divine', value: bonuses.divine},
            {label: 'Divine (Luxury)', value: bonuses.luxuryDivine},
            {label: 'Occult', value: bonuses.occult},
            {label: 'Occult (Luxury)', value: bonuses.luxuryOccult},
            {label: 'Primal', value: bonuses.primal},
            {label: 'Primal (Luxury)', value: bonuses.luxuryPrimal},
            {label: 'Other', value: bonuses.other},
        ];
    }

    private getStorage(structures: StructureResult): LabeledData[] {
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
                                label: unslugify(action),
                                value: value,
                            };
                        }),
                };
            });
    }
}

export async function showSettlement(game: Game, settlementId: string, kingdom: Kingdom): Promise<void> {
    new SettlementApp({game, settlementId, kingdom}).render(true);
}
