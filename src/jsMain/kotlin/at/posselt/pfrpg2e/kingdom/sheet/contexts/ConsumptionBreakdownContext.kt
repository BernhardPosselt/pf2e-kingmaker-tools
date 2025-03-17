package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.kingdom.resources.Consumption
import js.objects.JsPlainObject

@Suppress("unused")
@JsPlainObject
external interface ConsumptionBreakdownContext {
    val total: Int
    val settlementSurplus: Int
    val farmlands: Int
    val food: Int
    val armies: Int
    val settlements: Int
    val resourcesSurplus: Int
    val now: Int
}

fun Consumption.toContext() = ConsumptionBreakdownContext(
    total = total,
    settlementSurplus = settlementSurplus,
    farmlands = farmlands,
    food = food,
    armies = armies,
    settlements = settlements,
    resourcesSurplus = resourcesSurplus,
    now = now,
)