package at.posselt.pfrpg2e.kingdom.data

import at.posselt.pfrpg2e.data.kingdom.structures.CommodityStorage
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RawCommodities {
    var food: Int
    var lumber: Int
    var luxuries: Int
    var ore: Int
    var stone: Int
}

@JsPlainObject
external interface RawCurrentCommodities {
    var now: RawCommodities
    var next: RawCommodities
}

fun RawCommodities.limitBy(storage: CommodityStorage) =
    RawCommodities(
        food = food.coerceIn(0, storage.food),
        lumber = lumber.coerceIn(0, storage.lumber),
        luxuries = luxuries.coerceIn(0, storage.luxuries),
        ore = ore.coerceIn(0, storage.ore),
        stone = stone.coerceIn(0, storage.stone),
    )

fun RawCurrentCommodities.endTurn(storage: CommodityStorage) = RawCurrentCommodities(
    now = RawCommodities(
        food = now.food + next.food,
        lumber = now.lumber + next.lumber,
        luxuries = now.luxuries + next.luxuries,
        ore = now.ore + next.ore,
        stone = now.stone + next.stone,
    ).limitBy(storage),
    next = RawCommodities(0, 0, 0, 0, 0),
)