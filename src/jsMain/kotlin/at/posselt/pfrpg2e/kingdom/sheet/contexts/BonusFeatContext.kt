package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.kingdom.RawFeat
import at.posselt.pfrpg2e.kingdom.RawGovernment
import at.posselt.pfrpg2e.kingdom.RawRuinThresholdIncreases
import at.posselt.pfrpg2e.kingdom.data.RawBonusFeat
import at.posselt.pfrpg2e.kingdom.data.RawFeatureChoices
import js.objects.JsPlainObject

@JsPlainObject
external interface AddBonusFeatContext {
    val feat: FormElementContext
    val description: String
    val name: String
}

@JsPlainObject
external interface BonusFeatContext {
    val id: FormElementContext
    val idValue: String
    val name: String
    val description: String
    val automationNotes: String?
    val ruinThresholdIncreases: Array<RuinThresholdIncreases>
}

fun createBonusFeatContext(
    government: RawGovernment?,
    feats: Array<RawFeat>,
    choices: Array<RawFeatureChoices>,
    bonusFeats: Array<RawBonusFeat>,
    value: String?,
    trainedSkills: Set<KingdomSkill>,
): AddBonusFeatContext {
    val takenFeats = getTakenFeats(choices, government, bonusFeats, trainedSkills)
    val feat = feats.find { it.id == value }
    return AddBonusFeatContext(
        feat = Select(
            name = "bonusFeat",
            label = "Feat",
            value = value,
            options = feats
                .filter { it.id == value || it.id !in takenFeats }
                .map { SelectOption(it.name, it.id) },
            required = false,
            stacked = false,
            hideLabel = true,
        ).toContext(),
        description = feat?.text ?: "",
        name = feat?.name ?: "",
    )
}

fun Array<RawBonusFeat>.toContext(
    feats: Array<RawFeat>,
): Array<BonusFeatContext> {
    val featsById = feats.associateBy { it.id }
    return mapIndexedNotNull { index, rawFeat ->
        featsById[rawFeat.id]?.let { feat ->
            val ruinThresholdIncreases: Array<RawRuinThresholdIncreases> = feat.ruinThresholdIncreases ?: emptyArray()
            BonusFeatContext(
                id = HiddenInput(name = "bonusFeats.$index.id", value = feat.id).toContext(),
                idValue = feat.id,
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