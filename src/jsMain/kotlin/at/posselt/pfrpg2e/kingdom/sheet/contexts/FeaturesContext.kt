package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.OverrideType
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.data.actor.findHighestProficiency
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.kingdom.RawExplodedKingdomFeature
import at.posselt.pfrpg2e.kingdom.RawKingdomFeat
import at.posselt.pfrpg2e.kingdom.data.RawFeatureChoices
import at.posselt.pfrpg2e.kingdom.data.RawRuinThresholdIncrease
import at.posselt.pfrpg2e.kingdom.data.RawRuinThresholdIncreases
import js.objects.JsPlainObject

@JsPlainObject
external interface RuinThresholdIncrease {
    val valueInput: FormElementContext
    val input: FormElementContext
}

@JsPlainObject
external interface RuinThresholdIncreases {
    val amount: Int
    val value: Int
    val crime: RuinThresholdIncrease
    val corruption: RuinThresholdIncrease
    val strife: RuinThresholdIncrease
    val stability: RuinThresholdIncrease
}

@JsPlainObject
external interface FeatureContext {
    val id: FormElementContext
    val hidden: Boolean
    val level: Int
    val name: String
    val description: String
    val highestProficiency: String
    val abilityBoosts: AbilityBoostContext?
    val feat: FormElementContext?
    val featDescription: String?
    val skillProficiency: FormElementContext?
    val ruinThresholdIncreases: RuinThresholdIncreases?
    val featRuinThresholdIncreases: Array<RuinThresholdIncreases>
}

fun RawRuinThresholdIncrease.toContext(
    prefix: String,
    label: String,
) = RuinThresholdIncrease(
    valueInput = HiddenInput(
        name = "$prefix.value",
        value = value.toString(),
        overrideType = OverrideType.NUMBER
    ).toContext(),
    input = CheckboxInput(
        name = "$prefix.increase",
        value = increase,
        label = label,
        stacked = true
    ).toContext(),
)

fun RawRuinThresholdIncreases.toContext(prefix: String, amount: Int): RuinThresholdIncreases =
    RuinThresholdIncreases(
        value = crime.value,
        amount = amount,
        crime = crime.toContext("$prefix.crime", "Crime"),
        corruption = corruption.toContext("$prefix.corruption", "Corruption"),
        strife = strife.toContext("$prefix.strife", "Strife"),
        stability = stability.toContext("$prefix.stability", "Stability"),
    )


private fun defaultRuinThresholdIncrease(value: Int) =
    RawRuinThresholdIncreases(
        crime = RawRuinThresholdIncrease(
            value = value,
            increase = false,
        ),
        corruption = RawRuinThresholdIncrease(
            value = value,
            increase = false,
        ),
        strife = RawRuinThresholdIncrease(
            value = value,
            increase = false,
        ),
        stability = RawRuinThresholdIncrease(
            value = value,
            increase = false,
        ),
    )

fun Array<RawFeatureChoices>.toContext(
    kingdomLevel: Int,
    features: Array<RawExplodedKingdomFeature>,
    choices: Array<RawFeatureChoices>,
    feats: Array<RawKingdomFeat>,
    increaseBoostsBy: Int,
): Array<FeatureContext> {
    val featsById = feats.associateBy { it.id }
    val choicesById = choices.associateBy { it.id }
    return features.mapIndexed { index, feature ->
        val choice = choicesById[feature.id]
        val feat = choice?.featId?.let { featsById[it] }
        val featSelectOptions = feats.map { SelectOption("${it.name} (${it.level})", it.id) }
        val skillIncreaseOptions = KingdomSkill.entries.map { SelectOption(it.label, it.value) }
        val ruinThresholdIncreases = feature.ruinThresholdIncreases
        FeatureContext(
            id = HiddenInput(name = "features.$index.id", value = feature.id).toContext(),
            level = feature.level,
            hidden = feature.level < kingdomLevel,
            name = feature.name,
            description = feature.description,
            highestProficiency = findHighestProficiency(feature.level)?.label ?: "Untrained",
            abilityBoosts = choice?.abilityBoosts?.toContext("features.$index", 2 + increaseBoostsBy),
            feat = Select(
                label = "Kingdom Feat",
                name = "features.$index.featId",
                value = feat?.id,
                options = featSelectOptions,
                required = false,
            ).toContext(),
            featDescription = feat?.text ?: "",
            skillProficiency = Select(
                label = "Skill Increase",
                name = "features.$index.skillIncrease",
                value = feat?.id,
                options = skillIncreaseOptions,
                required = false,
            ).toContext(),
            ruinThresholdIncreases = if (choice == null && ruinThresholdIncreases != null) {
                defaultRuinThresholdIncrease(ruinThresholdIncreases.increase)
                    .toContext("features.$index.ruinThresholdIncreases", ruinThresholdIncreases.amount)
            } else {
                choice?.ruinThresholdIncreases
                    ?.let {
                        it.toContext(
                            "features.$index.ruinThresholdIncreases",
                            ruinThresholdIncreases?.amount ?: 2
                        )
                    }
            },
            featRuinThresholdIncreases = feat?.ruinThresholdIncreases?.mapIndexed { ruinIndex, value ->
                defaultRuinThresholdIncrease(value.increase)
                    .toContext("features.$index.featRuinThresholdIncreases.$ruinIndex", value.amount)
            }?.toTypedArray() ?: emptyArray(),
        )
    }.toTypedArray()
}
