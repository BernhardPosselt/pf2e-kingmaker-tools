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
import at.posselt.pfrpg2e.kingdom.data.RawAbilityBoostChoices
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
external interface FeatureByLevelContext {
    val hidden: Boolean
    val level: Int
    val features: Array<FeatureContext>
}

@JsPlainObject
external interface FeatureContext {
    val id: FormElementContext
    val name: String
    val description: String
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
): Array<FeatureByLevelContext> {
    val featsById = feats.associateBy { it.id }
    val choicesById = choices.associateBy { it.id }
    val skillIncreaseOptions = KingdomSkill.entries.map { SelectOption(it.label, it.value) }
    return features
        .groupBy { it.level }
        .map { (level, f) ->
            val highestProficiency = findHighestProficiency(level)?.label ?: "Untrained"
            FeatureByLevelContext(
                level = level,
                hidden = level > kingdomLevel,
                features = f.mapIndexed { index, feature ->
                    val choice = choicesById[feature.id]
                    val feat = choice?.featId?.let { featsById[it] }
                    val featSelectOptions = feats.map { SelectOption("${it.name} (${it.level})", it.id) }
                    val ruinThresholdIncreases = feature.ruinThresholdIncreases
                    val abilityBoosts = feature.abilityBoosts
                    FeatureContext(
                        id = HiddenInput(name = "features.$index.id", value = feature.id).toContext(),
                        name = feature.name,
                        description = feature.description,
                        abilityBoosts = if (abilityBoosts != null) {
                            val free = abilityBoosts + increaseBoostsBy
                            val boosts = choice?.abilityBoosts ?: RawAbilityBoostChoices(
                                culture = false,
                                economy = false,
                                loyalty = false,
                                stability = false,
                            )
                            boosts.toContext("features.$index", free)
                        } else {
                            null
                        },
                        feat = if (feature.kingdomFeat == true) {
                            Select(
                                label = "Kingdom Feat",
                                name = "features.$index.featId",
                                value = feat?.id,
                                options = featSelectOptions,
                                required = false,
                                stacked = false,
                            ).toContext()
                        } else {
                            null
                        },
                        featDescription = feat?.text ?: "",
                        skillProficiency = if (feature.skillIncrease == true) {
                            Select(
                                label = "Skill Increase (up to $highestProficiency)",
                                name = "features.$index.skillIncrease",
                                value = choice?.skillIncrease,
                                options = skillIncreaseOptions,
                                required = false,
                                stacked = false,
                            ).toContext()
                        } else {
                            null
                        },
                        ruinThresholdIncreases = if (choice == null && ruinThresholdIncreases != null) {
                            defaultRuinThresholdIncrease(ruinThresholdIncreases.increase)
                                .toContext("features.$index.ruinThresholdIncreases", ruinThresholdIncreases.amount)
                        } else {
                            choice?.ruinThresholdIncreases?.toContext(
                                "features.$index.ruinThresholdIncreases",
                                ruinThresholdIncreases?.amount ?: 2
                            )
                        },
                        featRuinThresholdIncreases = feat?.ruinThresholdIncreases?.mapIndexed { ruinIndex, value ->
                            defaultRuinThresholdIncrease(value.increase)
                                .toContext("features.$index.featRuinThresholdIncreases.$ruinIndex", value.amount)
                        }?.toTypedArray() ?: emptyArray(),
                    )
                }
                    .sortedBy { it.name }
                    .toTypedArray()
            )
        }.toTypedArray()

}
