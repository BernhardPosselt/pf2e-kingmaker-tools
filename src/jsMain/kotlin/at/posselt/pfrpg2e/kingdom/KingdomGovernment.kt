package at.posselt.pfrpg2e.kingdom

import kotlinx.js.JsPlainObject
import kotlinx.serialization.json.JsonElement

@JsPlainObject
external interface RawGovernment {
    val name: String
    val description: String
    val boosts: Array<String>
    val freeBoosts: Int
    val bonusFeat: String
    val skillProficiencies: Array<String>
}

@JsModule("./governments.json")
external val governments: Array<RawGovernment>

@JsModule("./schemas/government.json")
external val governmentSchema: JsonElement