package at.posselt.pfrpg2e.kingdom.data

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