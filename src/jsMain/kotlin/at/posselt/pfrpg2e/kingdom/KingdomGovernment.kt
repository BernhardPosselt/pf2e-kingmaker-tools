package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.utils.t
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

private fun RawGovernment.translate() =
    RawGovernment.copy(
        this,
        name = t(name),
        description = t(description)
    )

private var translatedGovernments = emptyArray<RawGovernment>()

fun translateGovernments() {
    translatedGovernments = governments
        .map { it.translate() }
        .toTypedArray()
}

fun KingdomData.getGovernments(): Array<RawGovernment> {
    val overrides = homebrewGovernments.map { it.id }.toSet()
    return homebrewGovernments + translatedGovernments.filter { it.id !in overrides }
}