package at.posselt.pfrpg2e.kingdom

import kotlinx.js.JsPlainObject
import kotlinx.serialization.json.JsonElement

@JsPlainObject
external interface RawCharter {
    val id: String
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


fun KingdomData.getCharters(): Array<RawCharter> {
    val overrides = homebrewCharters.map { it.id }.toSet()
    return homebrewCharters + charters.filter { it.id !in overrides }
}