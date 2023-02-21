import {CommodityStorage} from './data/structures';
import {Commodities, getSizeData, Kingdom} from './data/kingdom';
import {getAllMergedSettlements} from './scene';

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
    const commodityCapacity = getSizeData(kingdom.size).commodityCapacity;
    const {storage} = getAllMergedSettlements(game, kingdom);
    return calculateStorageCapacity(commodityCapacity, storage);
}

export function getConsumption(game: Game, kingdom: Kingdom): number {
    const settlementConsumption = getAllMergedSettlements(game, kingdom).settlementConsumption;
    return Math.max(0, kingdom.consumption.armies + kingdom.consumption.now + settlementConsumption);
}
