package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.OverrideType
import at.posselt.pfrpg2e.kingdom.data.RawAbilityBoostChoices
import js.objects.JsPlainObject

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
    overrideCulture: Boolean? = null,
    overrideEconomy: Boolean? = null,
    overrideLoyalty: Boolean? = null,
    overrideStability: Boolean? = null,
) = AbilityBoostContext(
    free = free,
    cultureHidden = overrideCulture != null,
    economyHidden = overrideEconomy != null,
    loyaltyHidden = overrideLoyalty != null,
    stabilityHidden = overrideStability != null,
    culture = if (overrideCulture == null) {
        CheckboxInput(
            name = "$prefix.abilityBoosts.culture",
            value = culture,
            label = "Culture",
            stacked = true
        )
    } else {
        HiddenInput(
            value = overrideCulture.toString(),
            overrideType = OverrideType.BOOLEAN,
            name = "$prefix.abilityBoosts.culture"
        )
    }.toContext(),
    economy = if (overrideEconomy == null) {
        CheckboxInput(
            name = "$prefix.abilityBoosts.economy",
            value = economy,
            label = "Economy",
            stacked = true
        )
    } else {
        HiddenInput(
            value = overrideEconomy.toString(),
            overrideType = OverrideType.BOOLEAN,
            name = "$prefix.abilityBoosts.economy"
        )
    }.toContext(),
    loyalty = if (overrideLoyalty == null) {
        CheckboxInput(
            name = "$prefix.abilityBoosts.loyalty",
            value = loyalty,
            label = "Loyalty",
            stacked = true
        )
    } else {
        HiddenInput(
            value = overrideLoyalty.toString(),
            overrideType = OverrideType.BOOLEAN,
            name = "$prefix.abilityBoosts.loyalty"
        )
    }.toContext(),
    stability = if (overrideStability == null) {
        CheckboxInput(
            name = "$prefix.abilityBoosts.stability",
            value = stability,
            label = "Stability",
            stacked = true
        )
    } else {
        HiddenInput(
            value = overrideStability.toString(),
            overrideType = OverrideType.BOOLEAN,
            name = "$prefix.abilityBoosts.stability"
        )
    }.toContext(),
)