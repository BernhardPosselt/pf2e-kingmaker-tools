package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.OverrideType
import at.posselt.pfrpg2e.data.kingdom.KingdomAbility
import at.posselt.pfrpg2e.kingdom.data.RawAbilityBoostChoices
import at.posselt.pfrpg2e.utils.t
import kotlinx.js.JsPlainObject

@Suppress("unused")
@JsPlainObject
external interface AbilityBoostContext {
    val free: Int
    val cultureHidden: Boolean
    val economyHidden: Boolean
    val loyaltyHidden: Boolean
    val stabilityHidden: Boolean
    val culture: FormElementContext
    val economy: FormElementContext
    val loyalty: FormElementContext
    val stability: FormElementContext
}

fun RawAbilityBoostChoices.toContext(
    prefix: String,
    free: Int,
    disableCulture: Boolean = false,
    disableEconomy: Boolean = false,
    disableLoyalty: Boolean = false,
    disableStability: Boolean = false,
) = AbilityBoostContext(
    free = free,
    cultureHidden = disableCulture,
    economyHidden = disableEconomy,
    loyaltyHidden = disableLoyalty,
    stabilityHidden = disableStability,
    culture = if (disableCulture) {
        HiddenInput(
            value = "false",
            overrideType = OverrideType.BOOLEAN,
            name = "${prefix}abilityBoosts.culture"
        )
    } else {
        CheckboxInput(
            name = "${prefix}abilityBoosts.culture",
            value = culture,
            label = t(KingdomAbility.CULTURE),
            stacked = true
        )
    }.toContext(),
    economy = if (disableEconomy) {
        HiddenInput(
            value = "false",
            overrideType = OverrideType.BOOLEAN,
            name = "${prefix}abilityBoosts.economy"
        )
    } else {
        CheckboxInput(
            name = "${prefix}abilityBoosts.economy",
            value = economy,
            label = t(KingdomAbility.ECONOMY),
            stacked = true
        )
    }.toContext(),
    loyalty = if (disableLoyalty) {
        HiddenInput(
            value = "false",
            overrideType = OverrideType.BOOLEAN,
            name = "${prefix}abilityBoosts.loyalty"
        )
    } else {
        CheckboxInput(
            name = "${prefix}abilityBoosts.loyalty",
            value = loyalty,
            label = t(KingdomAbility.LOYALTY),
            stacked = true
        )
    }.toContext(),
    stability = if (disableStability) {
        HiddenInput(
            value = "false",
            overrideType = OverrideType.BOOLEAN,
            name = "${prefix}abilityBoosts.stability"
        )
    } else {
        CheckboxInput(
            name = "${prefix}abilityBoosts.stability",
            value = stability,
            label = t(KingdomAbility.STABILITY),
            stacked = true
        )
    }.toContext(),
)