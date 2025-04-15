package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.data.kingdom.structures.CommodityStorage
import at.posselt.pfrpg2e.kingdom.data.RawCommodities
import at.posselt.pfrpg2e.kingdom.data.RawCurrentCommodities
import at.posselt.pfrpg2e.kingdom.sheet.Turn
import at.posselt.pfrpg2e.utils.t
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

@Suppress("unused")
@JsPlainObject
external interface CommoditiesContext {
    val now: CommodityContext
    val next: CommodityContext
    val capacity: CapacityContext
}

fun RawCommodities.toContext(round: Turn, capacity: CommodityStorage) =
    CommodityContext(
        food = Select.range(
            from = 0,
            to = capacity.food,
            value = food,
            name = "commodities.${round.value}.food",
            label = if (round == Turn.NOW) t("kingdom.food") else t(round),
            elementClasses = listOf("km-width-small"),
            labelClasses = listOf("km-slim-inputs"),
            stacked = false,
        ).toContext(),
        lumber = Select.range(
            from = 0,
            to = capacity.lumber,
            value = lumber,
            name = "commodities.${round.value}.lumber",
            label = if (round == Turn.NOW) t("kingdom.lumber") else t(round),
            elementClasses = listOf("km-width-small"),
            labelClasses = listOf("km-slim-inputs"),
            stacked = false,
        ).toContext(),
        luxuries = Select.range(
            from = 0,
            to = capacity.luxuries,
            value = luxuries,
            name = "commodities.${round.value}.luxuries",
            label = if (round == Turn.NOW) t("kingdom.luxuries") else t(round),
            elementClasses = listOf("km-width-small"),
            labelClasses = listOf("km-slim-inputs"),
            stacked = false,
        ).toContext(),
        ore = Select.range(
            from = 0,
            to = capacity.ore,
            value = ore,
            name = "commodities.${round.value}.ore",
            label = if (round == Turn.NOW) t("kingdom.ore") else t(round),
            elementClasses = listOf("km-width-small"),
            labelClasses = listOf("km-slim-inputs"),
            stacked = false,
        ).toContext(),
        stone = Select.range(
            from = 0,
            to = capacity.stone,
            value = stone,
            name = "commodities.${round.value}.stone",
            label = if (round == Turn.NOW) t("kingdom.stone") else t(round),
            elementClasses = listOf("km-width-small"),
            labelClasses = listOf("km-slim-inputs"),
            stacked = false,
        ).toContext(),
    )



fun RawCurrentCommodities.toContext(capacity: CommodityStorage) =
    CommoditiesContext(
        now = now.toContext(Turn.NOW, capacity),
        next = next.toContext(Turn.NEXT, capacity),
        capacity = CapacityContext(
            ore = capacity.ore,
            food = capacity.food,
            lumber = capacity.lumber,
            stone = capacity.stone,
            luxuries = capacity.luxuries,
        ),
    )