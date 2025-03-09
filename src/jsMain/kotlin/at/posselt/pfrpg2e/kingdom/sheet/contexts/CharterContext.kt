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
    val boost: String?
    val flaw: String?
}

fun RawCharterChoices.toContext(charters: Array<RawCharter>): CharterContext {
    val charter = charters.find { it.id == type }
    val charterBoost = charter?.boost
    return CharterContext(
        type = Select(
            name = "charter.type",
            value = type,
            options = charters.map { SelectOption(it.name, it.id) },
            label = "Charter",
            required = false,
            stacked = false,
            hideLabel = true,
        ).toContext(),
        abilityBoosts = abilityBoosts.toContext(
            prefix = "charter",
            free = charter?.freeBoosts ?: 0,
            disableCulture = charterBoost == "culture",
            disableEconomy = charterBoost == "economy",
            disableLoyalty = charterBoost == "loyalty",
            disableStability = charterBoost == "stability",
        ),
        description = charter?.description ?: "",
        flaw = charter?.flaw,
        boost = charterBoost,
    )
}