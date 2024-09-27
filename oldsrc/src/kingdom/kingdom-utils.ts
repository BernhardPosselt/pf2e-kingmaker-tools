import {CommodityStorage} from './data/structures';
import {Commodities, getSizeData, Kingdom} from './data/kingdom';
import {getAllMergedSettlements, getStolenLandsData, ResourceAutomationMode} from './scene';
import {clamped} from '../utils';
import {getStringSetting} from '../settings';

function calculateStorageCapacity(capacity: number, storage: CommodityStorage): Commodities {
    return {
        food: capacity + storage.food,
        ore: capacity + storage.ore,
        luxuries: capacity + storage.luxuries,
        lumber: capacity + storage.lumber,
        stone: capacity + storage.stone,
    };
}

export function getCapacity(game: Game, kingdom: Kingdom): Commodities {
    const automateResourceMode = getStringSetting(game, 'automateResources') as ResourceAutomationMode;
    const {size: kingdomSize} = getStolenLandsData(game, automateResourceMode, kingdom);
    const commodityCapacity = getSizeData(kingdomSize).commodityCapacity;
    const {storage} = getAllMergedSettlements(game, kingdom);
    return calculateStorageCapacity(commodityCapacity, storage);
}

export function getConsumption(game: Game, kingdom: Kingdom): { current: number, surplus: number } {
    const allMergedSettlements = getAllMergedSettlements(game, kingdom);
    const settlementConsumption = allMergedSettlements.settlementConsumption;
    const resourceMode = getStringSetting(game, 'automateResources') as ResourceAutomationMode;
    const farmlands = getStolenLandsData(game, resourceMode, kingdom).workSites.farmlands;
    const farmlandsCount = farmlands.resources + farmlands.quantity;
    const combinedConsumption = kingdom.consumption.armies + kingdom.consumption.now + settlementConsumption;
    const current = Math.max(0, combinedConsumption - farmlandsCount);
    const surplus = Math.max(0, farmlandsCount - combinedConsumption);
    return {current, surplus};
}

export function gainFame(kingdom: Kingdom, fame: number): Partial<Kingdom> {
    return {
        fame: {
            ...kingdom.fame,
            now: clamped(kingdom.fame.now + fame, 0, kingdom.fame.max),
        },
    };
}
