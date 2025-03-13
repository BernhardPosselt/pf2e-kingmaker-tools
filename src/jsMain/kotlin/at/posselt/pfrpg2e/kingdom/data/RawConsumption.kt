package at.posselt.pfrpg2e.kingdom.data

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RawConsumption {
    var armies: Int
    var now: Int
    var next: Int
}

fun RawConsumption.endTurn() = RawConsumption(
    now = next,
    next = 0,
    armies = armies,
)