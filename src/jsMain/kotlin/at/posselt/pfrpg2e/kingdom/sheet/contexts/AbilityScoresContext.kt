package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.kingdom.data.RawAbilityScores
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface AbilityScoresContext {
    val economy: FormElementContext
    val stability: FormElementContext
    val loyalty: FormElementContext
    val culture: FormElementContext
}

fun RawAbilityScores.toContext() =
    AbilityScoresContext(
        economy= NumberInput(
            name = "abilityScores.economy",
            label = "Economy",
            hideLabel = true,
            value = economy
        ).toContext(),
        stability= NumberInput(
            name = "abilityScores.stability",
            label = "Stability",
            hideLabel = true,
            value = stability
        ).toContext(),
        loyalty= NumberInput(
            name = "abilityScores.loyalty",
            label = "Loyalty",
            hideLabel = true,
            value = loyalty
        ).toContext(),
        culture= NumberInput(
            name = "abilityScores.culture",
            label = "Culture",
            hideLabel = true,
            value = culture
        ).toContext(),
    )