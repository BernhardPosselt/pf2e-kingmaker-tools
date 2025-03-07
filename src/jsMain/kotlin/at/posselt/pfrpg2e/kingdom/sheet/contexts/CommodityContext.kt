package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.data.kingdom.structures.CommodityStorage
import at.posselt.pfrpg2e.kingdom.data.RawCommodities
import at.posselt.pfrpg2e.kingdom.data.RawCurrentCommodities
import at.posselt.pfrpg2e.toLabel
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface CommodityContext {
    val food: FormElementContext
    val lumber: FormElementContext
    val luxuries: FormElementContext
    val ore: FormElementContext
    val stone: FormElementContext
}

@JsPlainObject
external interface CapacityContext {
    val ore: Int
    val food: Int
    val lumber: Int
    val stone: Int
    val luxuries: Int
}

@JsPlainObject
external interface CommoditiesContext {
    val now: CommodityContext
    val next: CommodityContext
    val capacity: CapacityContext
}

fun RawCommodities.toContext(round: String, capacity: CommodityStorage) =
    CommodityContext(
        food = Select.range(
            from = 0,
            to = capacity.food,
            value = food,
            name = "commodities.$round.food",
            label = round.toLabel()
        ).toContext(),
        lumber = Select.range(
            from = 0,
            to = capacity.lumber,
            value = lumber,
            name = "commodities.$round.lumber",
            label = round.toLabel()
        ).toContext(),
        luxuries = Select.range(
            from = 0,
            to = capacity.luxuries,
            value = luxuries,
            name = "commodities.$round.luxuries",
            label = round.toLabel()
        ).toContext(),
        ore = Select.range(
            from = 0,
            to = capacity.ore,
            value = ore,
            name = "commodities.$round.ore",
            label = round.toLabel()
        ).toContext(),
        stone = Select.range(
            from = 0,
            to = capacity.stone,
            value = stone,
            name = "commodities.$round.stone",
            label = round.toLabel()
        ).toContext(),
    )



fun RawCurrentCommodities.toContext(capacity: CommodityStorage) =
    CommoditiesContext(
        now = now.toContext("now", capacity),
        next = next.toContext("next", capacity),
        capacity = CapacityContext(
            ore = capacity.ore,
            food = capacity.food,
            lumber = capacity.lumber,
            stone = capacity.stone,
            luxuries = capacity.luxuries,
        ),
    )