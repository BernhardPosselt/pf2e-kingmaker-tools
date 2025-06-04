package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.utils.t
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RawCharter {
    val id: String
    val name: String
    val description: String
    val flaw: String?
    val freeBoosts: Int
    val boost: String?
}

@JsModule("./charters.json")
external val charters: Array<RawCharter>

private fun RawCharter.translate() =
    RawCharter.copy(
        this,
        name = t(name),
        description = t(description)
    )

private var translatedCharters = emptyArray<RawCharter>()

fun translateCharters() {
    translatedCharters = charters
        .map { it.translate() }
        .toTypedArray()
}

fun KingdomData.getCharters(): Array<RawCharter> {
    val overrides = homebrewCharters.map { it.id }.toSet()
    return homebrewCharters + translatedCharters.filter { it.id !in overrides }
}