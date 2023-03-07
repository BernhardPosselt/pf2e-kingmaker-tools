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
    const farmlands = kingdom.workSites.farmlands.resources + kingdom.workSites.farmlands.quantity;
    return Math.max(0, kingdom.consumption.armies + kingdom.consumption.now + settlementConsumption -
        farmlands);
}

export function gainFame(kingdom: Kingdom, fame: number): Partial<Kingdom> {
    return {
        fame: {
            ...kingdom.fame,
            now: Math.min(3, kingdom.fame.now + fame),
        },
    };
}
