package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.kingdom.RawCharter
import at.posselt.pfrpg2e.kingdom.data.RawCharterChoices
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface CharterContext {
    val type: FormElementContext
    val description: String
    val abilityBoosts: AbilityBoostContext
    val flaw: String
}

fun RawCharterChoices.toContext(charters: Array<RawCharter>): CharterContext {
    val charter = charters.find { it.id == type }
    return CharterContext(
        type = Select(
            name = "charter.type",
            value = type,
            options = charters.map { SelectOption(it.name, it.id) },
            label = "Charter",
            required = false,
        ).toContext(),
        abilityBoosts = abilityBoosts.toContext("charter", charter?.freeBoosts ?: 0),
        description =  charter?.description ?: "",
        flaw = charter?.flaw ?: "",
    )
}