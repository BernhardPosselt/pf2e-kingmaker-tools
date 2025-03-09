package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.kingdom.RawGovernment
import at.posselt.pfrpg2e.kingdom.RawKingdomFeat
import at.posselt.pfrpg2e.kingdom.RawRuinThresholdIncreases
import at.posselt.pfrpg2e.kingdom.data.RawBonusFeat
import at.posselt.pfrpg2e.kingdom.data.RawFeatureChoices
import js.objects.JsPlainObject

@JsPlainObject
external interface AddBonusFeatContext {
    val feat: FormElementContext
    val description: String
}

@JsPlainObject
external interface BonusFeatContext {
    val id: FormElementContext
    val name: String
    val description: String
    val automationNotes: String?
    val ruinThresholdIncreases: Array<RuinThresholdIncreases>
}

fun createBonusFeatContext(
    government: RawGovernment?,
    feats: Array<RawKingdomFeat>,
    choices: Array<RawFeatureChoices>,
    bonusFeats: Array<RawBonusFeat>,
    value: String?,
): AddBonusFeatContext {
    val takenFeats = getTakenFeats(choices, government, bonusFeats)
    return AddBonusFeatContext(
        feat = Select(
            name = "bonusFeat.feat",
            label = "Feat",
            value = value,
            options = feats
                .filter { it.id == value || it.id !in takenFeats }
                .map { SelectOption(it.name, it.id) },
            required = false,
            stacked = false,
        ).toContext(),
        description = feats.find { it.id == value }?.text ?: "",
    )
}

fun Array<RawBonusFeat>.toContext(
    feats: Array<RawKingdomFeat>,
): Array<BonusFeatContext> {
    val featsById = feats.associateBy { it.id }
    return mapIndexedNotNull { index, rawFeat ->
        featsById[rawFeat.id]?.let { feat ->
            val ruinThresholdIncreases: Array<RawRuinThresholdIncreases> = feat.ruinThresholdIncreases ?: emptyArray()
            BonusFeatContext(
                id = HiddenInput(name = "bonusFeats.$index.id", value = feat.id).toContext(),
                name = feat.name,
                description = feat.text,
                automationNotes = feat.automationNotes,
                ruinThresholdIncreases = ruinThresholdIncreases.mapIndexed { ruinIndex, value ->
                    defaultRuinThresholdIncrease(value.increase)
                        .toContext("features.$index.featRuinThresholdIncreases.$ruinIndex", value.amount)
                }.toTypedArray(),
            )
        }
    }.toTypedArray()
}