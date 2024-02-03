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

export function getConsumption(game: Game, kingdom: Kingdom): number {
    const settlementConsumption = getAllMergedSettlements(game, kingdom).settlementConsumption;
    const resourceMode = getStringSetting(game, 'automateResources') as ResourceAutomationMode;
    const farmlands = getStolenLandsData(game, resourceMode, kingdom).workSites.farmlands;
    const farmlandsCount = farmlands.resources + farmlands.quantity;
    return Math.max(0, kingdom.consumption.armies + kingdom.consumption.now + settlementConsumption -
        farmlandsCount);
}

export function gainFame(kingdom: Kingdom, fame: number): Partial<Kingdom> {
    return {
        fame: {
            ...kingdom.fame,
            now: clamped(kingdom.fame.now + fame, 0, 3),
        },
    };
}
