import {ActivityBonuses, ItemLevelBonuses, SkillItemBonuses} from '../data/structures';
import {capitalize, createUUIDLink, unslugify} from '../../utils';
import {calculateAvailableItems, countStructureOccurrences, groupAvailableItems, StructureResult} from '../structures';
import {hasFeat, Kingdom} from '../data/kingdom';
import {
    ActorStructure,
    getCapitalSettlement,
    getSceneActorStructures,
    getSettlement,
    getSettlementInfo,
    getStructureResult,
    getStructureStackMode,
    SettlementAndScene,
} from '../scene';
import {getBooleanSetting} from '../../settings';
import {getKingdomActivitiesById} from '../data/activityData';

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

interface StructureList {
    link: string;
    occurrences: number;
}

type SettlementTab = 'status' | 'effects' | 'bonuses' | 'buildings' | 'shopping' | 'storage';

class SettlementApp extends Application<ApplicationOptions & SettlementOptions> {
    private kingdom: Kingdom;

    static override get defaultOptions(): ApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'settlement-app';
        options.title = 'Settlement';
        options.template = 'modules/pf2e-kingmaker-tools/templates/kingdom/settlement.hbs';
        options.classes = ['kingmaker-tools-app', 'settlement-app'];
        options.width = 400;
        options.height = 'auto';
        return options;
    }

    private readonly game: Game;
    private readonly settlementId: string;
    private nav: SettlementTab = 'status';

    constructor(options: Partial<ApplicationOptions> & SettlementOptions) {
        super(options);
        this.game = options.game;
        this.settlementId = options.settlementId;
        this.kingdom = options.kingdom;
    }

    override async getData(): Promise<object> {
        const settlement = getSettlement(this.game, this.kingdom, this.settlementId)!;
        const capital = getCapitalSettlement(this.game, this.kingdom);
        const structureStackMode = getStructureStackMode(this.game);
        const autoCalculateSettlementLevel = getBooleanSetting(this.game, 'autoCalculateSettlementLevel');
        const activities = getKingdomActivitiesById(this.kingdom.homebrewActivities);
        const structureData = getStructureResult(structureStackMode, autoCalculateSettlementLevel, activities, settlement, capital);
        const builtStructures = await this.getBuiltStructures(settlement);
        const storage = this.getStorage(structureData);
        const settlementInfo = getSettlementInfo(settlement, autoCalculateSettlementLevel);
        const skillItemBonuses = this.getSkillBonuses(structureData.skillBonuses);
        const hasEffects = structureData.notes.length > 0;
        const hasStorage = Object.keys(storage).length > 0;
        const hasBonuses = skillItemBonuses.map(b => b.value > 0 || b.actions.some(a => a.value > 0));
        const hasBuildings = builtStructures.length > 0;
        // reset active tab if not active anymore
        if (this.nav === 'effects' && !hasEffects) this.nav = 'status';
        if (this.nav === 'storage' && !hasStorage) this.nav = 'status';
        if (this.nav === 'bonuses' && !hasBonuses) this.nav = 'status';
        if (this.nav === 'buildings' && !hasBuildings) this.nav = 'status';
        return {
            name: settlement.scene.name,
            type: capitalize(settlement.settlement.type),
            secondaryTerritory: settlement.settlement.secondaryTerritory,
            hasBridge: structureData.hasBridge,
            waterBorders: settlement.settlement.waterBorders,
            ...settlementInfo,
            ...this.getActiveTabs(),
            config: structureData.config,
            builtStructures,
            hasStorage,
            hasEffects,
            hasBonuses,
            hasBuildings,
            overcrowded: settlementInfo.lots > structureData.residentialLots,
            residentialLots: structureData.residentialLots,
            consumption: structureData.consumption,
            consumptionSurplus: structureData.consumptionSurplus,
            capitalInvestmentPossible: structureData.allowCapitalInvestment ? 'yes' : 'no',
            settlementEventBonus: structureData.settlementEventBonus,
            leadershipActivityBonus: structureData.leadershipActivityBonus,
            notes: structureData.notes,
            leadershipActivities: structureData.increaseLeadershipActivities ? 3 : 2,
            availableItems: this.getAvailableItems(settlementInfo.level, structureData.itemLevelBonuses),
            storage,
            skillItemBonuses,
        };
    }

    public sceneChange(): void {
        this.render();
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        Hooks.on('createToken', this.sceneChange.bind(this));
        Hooks.on('sightRefresh', this.sceneChange.bind(this)); // end of drag movement
        Hooks.on('deleteToken', this.sceneChange.bind(this));
        const $html = html[0];
        $html.querySelectorAll('.km-nav a')?.forEach(el => {
            el.addEventListener('click', (event) => {
                const tab = event.currentTarget as HTMLAnchorElement;
                this.nav = tab.dataset.tab as SettlementTab;
                this.render();
            });
        });
    }

    override close(options?: FormApplication.CloseOptions): Promise<void> {
        Hooks.off('createToken', this.sceneChange);
        Hooks.off('sightRefresh', this.sceneChange); // end of drag movement
        Hooks.off('deleteToken', this.sceneChange);
        return super.close(options);
    }

    private getAvailableItems(settlementLevel: number, itemLevelBonuses: ItemLevelBonuses): ItemLevelBonusData[] {
        const qualityOfLifeBonus = hasFeat(this.kingdom, 'Quality of Life') ? 1 : 0;
        const bonuses = calculateAvailableItems(itemLevelBonuses, settlementLevel, qualityOfLifeBonus);
        const groupedBonuses = groupAvailableItems(bonuses);
        return (Array.from(Object.entries(groupedBonuses)) as [keyof ItemLevelBonuses, number][])
            .map(([key, value]) => {
                return {label: itemBonusLabels[key], value};
            });
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

    private async getBuiltStructures(settlement: SettlementAndScene): Promise<StructureList[]> {
        const sceneActorStructures = getSceneActorStructures(settlement.scene);
        const countedStructures = countStructureOccurrences(sceneActorStructures);
        return await Promise.all(Array.from(countedStructures.entries())
            .sort(([a], [b]) => a.localeCompare(b))
            .map(([, value]): [ActorStructure, number] => [value.item, value.count])
            .map(async ([structure, occurrences]) => {
                return {
                    link: await TextEditor.enrichHTML(createUUIDLink(structure.actor.uuid, structure.name)),
                    occurrences: occurrences,
                };
            }));
    }

    private getActiveTabs(): Record<string, boolean> {
        return {
            statusTab: this.nav === 'status',
            bonusesTab: this.nav === 'bonuses',
            effectsTab: this.nav === 'effects',
            shoppingTab: this.nav === 'shopping',
            buildingsTab: this.nav === 'buildings',
            storageTab: this.nav === 'storage',
        };
    }
}

export async function showSettlement(game: Game, settlementId: string, kingdom: Kingdom): Promise<void> {
    new SettlementApp({game, settlementId, kingdom}).render(true);
}

const itemBonusLabels: Record<keyof ItemLevelBonuses, string> = {
    'alchemical': 'Alchemical',
    'magical': 'Magical',
    'luxuryMagical': 'Magical (Luxury)',
    'arcane': 'Arcane',
    'luxuryArcane': 'Arcane (Luxury)',
    'divine': 'Divine',
    'luxuryDivine': 'Divine (Luxury)',
    'occult': 'Occult',
    'luxuryOccult': 'Occult (Luxury)',
    'primal': 'Primal',
    'luxuryPrimal': 'Primal (Luxury)',
    'other': 'Other',
};