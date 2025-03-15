package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.OverrideType
import at.posselt.pfrpg2e.data.kingdom.KingdomAbility
import at.posselt.pfrpg2e.data.kingdom.KingdomAbilityScores
import at.posselt.pfrpg2e.kingdom.data.RawAbilityScores
import at.posselt.pfrpg2e.utils.formatAsModifier
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface AbilityScoreContext {
    val input: FormElementContext
    val mod: String
    val label: String
    val value: Int
}

private fun toAbilityContext(
    ability: KingdomAbility,
    rawValue: Int,
    automated: Boolean,
    scores: KingdomAbilityScores,
) = AbilityScoreContext(
    input = if (automated) {
        HiddenInput(
            name = "abilityScores.${ability.value}",
            value = rawValue.toString(),
            overrideType = OverrideType.NUMBER,
        ).toContext()
    } else {
        NumberInput(
            name = "abilityScores.${ability.value}",
            label = ability.label,
            hideLabel = true,
            value = rawValue,
            elementClasses = listOf("km-slim-inputs", "km-width-small"),
        ).toContext()
    },
    mod = scores.resolveModifier(ability).formatAsModifier(),
    label = ability.label,
    value = scores.resolve(ability),
)

fun RawAbilityScores.toContext(
    scores: KingdomAbilityScores,
    automated: Boolean,
) = arrayOf(
    toAbilityContext(
        ability = KingdomAbility.CULTURE,
        rawValue = culture,
        automated = automated,
        scores = scores,
    ),
    toAbilityContext(
        ability = KingdomAbility.ECONOMY,
        rawValue = economy,
        automated = automated,
        scores = scores,
    ),
    toAbilityContext(
        ability = KingdomAbility.LOYALTY,
        rawValue = loyalty,
        automated = automated,
        scores = scores,
    ),
    toAbilityContext(
        ability = KingdomAbility.STABILITY,
        rawValue = stability,
        automated = automated,
        scores = scores,
    ),
)
