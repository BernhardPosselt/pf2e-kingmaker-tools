package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.kingdom.data.RawAbilityBoostChoices
import js.objects.JsPlainObject

@JsPlainObject
external interface AbilityBoostContext {
    val free: Int
    val culture: FormElementContext
    val economy: FormElementContext
    val loyalty: FormElementContext
    val stability: FormElementContext
}

fun RawAbilityBoostChoices.toContext(prefix: String, free: Int) =
    AbilityBoostContext(
        free = free,
        culture = CheckboxInput(
            name = "$prefix.abilityBoosts.culture",
            value = culture,
            label = "Culture",
            stacked = true
        ).toContext(),
        economy = CheckboxInput(
            name = "$prefix.abilityBoosts.economy",
            value = economy,
            label = "Economy",
            stacked = true
        ).toContext(),
        loyalty = CheckboxInput(
            name = "$prefix.abilityBoosts.loyalty",
            value = loyalty,
            label = "Loyalty",
            stacked = true
        ).toContext(),
        stability = CheckboxInput(
            name = "$prefix.abilityBoosts.stability",
            value = stability,
            label = "Stability",
            stacked = true
        ).toContext(),
    )