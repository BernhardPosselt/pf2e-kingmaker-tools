package at.posselt.pfrpg2e.kingdom

import kotlinx.js.JsPlainObject
import kotlinx.serialization.json.JsonElement

@JsPlainObject
external interface RawCharter {
    val name: String
    val description: String
    val flaw: String
    val freeBoosts: Int
    val boost: String
}

@JsModule("./charters.json")
external val charters: Array<RawCharter>

@JsModule("./schemas/charter.json")
external val charterSchema: JsonElement