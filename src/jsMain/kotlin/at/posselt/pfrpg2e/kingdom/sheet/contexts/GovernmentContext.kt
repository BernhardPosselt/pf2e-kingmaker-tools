package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.kingdom.RawGovernment
import at.posselt.pfrpg2e.kingdom.RawKingdomFeat
import at.posselt.pfrpg2e.kingdom.data.RawGovernmentChoices
import at.posselt.pfrpg2e.toLabel
import js.objects.JsPlainObject

@JsPlainObject
external interface GovernmentContext {
    val type: FormElementContext
    val description: String?
    val abilityBoosts: AbilityBoostContext
    val skills: String
    val feat: String
    val boosts: String
    val featDescription: String
}

fun RawGovernmentChoices.toContext(
    governments: Array<RawGovernment>,
    feats: Array<RawKingdomFeat>,
): GovernmentContext {
    val featsById = feats.associateBy { it.id }
    val government = governments.find { it.id == type }
    val feat = government?.bonusFeat?.let { featsById[it] }
    val governmentBoosts = government?.boosts
    return GovernmentContext(
        type = Select(
            name = "government.type",
            value = type,
            options = governments.map { SelectOption(it.name, it.id) },
            label = "Government",
            required = false,
            stacked = false,
            hideLabel = true,
        ).toContext(),
        boosts = governmentBoosts?.joinToString(", ") { it.toLabel() } ?: "",
        description = government?.description,
        skills = government?.skillProficiencies?.joinToString(", ") { it.toLabel() } ?: "",
        feat = feat?.name ?: "",
        featDescription = feat?.text ?: "",
        abilityBoosts = abilityBoosts.toContext(
            prefix = "government.",
            free = government?.freeBoosts ?: 0,
            disableCulture = governmentBoosts?.any { it == "culture" } == true,
            disableEconomy = governmentBoosts?.any { it == "economy" } == true,
            disableLoyalty = governmentBoosts?.any { it == "loyalty" } == true,
            disableStability = governmentBoosts?.any { it == "stability" } == true,
        )
    )
}