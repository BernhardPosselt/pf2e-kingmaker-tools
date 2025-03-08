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
    val skills: Array<String>
    val feat: String
    val featDescription: String
}

fun RawGovernmentChoices.toContext(
    governments: Array<RawGovernment>,
    feats: Array<RawKingdomFeat>,
): GovernmentContext {
    val featsById = feats.associateBy { it.id }
    val government = governments.find { it.id == type }
    val feat = government?.bonusFeat?.let { featsById[it] }
    return GovernmentContext(
        type = Select(
            name = "government.type",
            value = type,
            options = governments.map { SelectOption(it.name, it.id) },
            label = "Government",
            required = false,
        ).toContext(),
        description = government?.description,
        skills = government?.skillProficiencies?.map { it.toLabel() }?.toTypedArray() ?: emptyArray(),
        feat = feat?.name ?: "",
        featDescription = feat?.text ?: "",
        abilityBoosts = abilityBoosts.toContext("government", government?.freeBoosts ?: 0)
    )
}