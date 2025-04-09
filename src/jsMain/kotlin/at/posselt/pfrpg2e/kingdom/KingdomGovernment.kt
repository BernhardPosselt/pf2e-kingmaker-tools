package at.posselt.pfrpg2e.kingdom

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RawGovernment {
    val id: String
    val name: String
    val description: String
    val boosts: Array<String>
    val freeBoosts: Int
    val bonusFeat: String
    val skillProficiencies: Array<String>
}

@JsModule("./governments.json")
external val governments: Array<RawGovernment>

fun KingdomData.getGovernments(): Array<RawGovernment> {
    val overrides = homebrewGovernments.map { it.id }.toSet()
    return homebrewGovernments + governments.filter { it.id !in overrides }
}