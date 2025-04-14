package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.OverrideType
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.data.actor.Proficiency
import at.posselt.pfrpg2e.data.actor.highestProficiencyByLevel
import at.posselt.pfrpg2e.data.kingdom.KingdomAbilityScores
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRanks
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.kingdom.RawExplodedKingdomFeature
import at.posselt.pfrpg2e.kingdom.RawFeat
import at.posselt.pfrpg2e.kingdom.RawGovernment
import at.posselt.pfrpg2e.kingdom.data.ChosenFeat
import at.posselt.pfrpg2e.kingdom.data.RawAbilityBoostChoices
import at.posselt.pfrpg2e.kingdom.data.RawBonusFeat
import at.posselt.pfrpg2e.kingdom.data.RawFeatureChoices
import at.posselt.pfrpg2e.kingdom.data.RawRuinThresholdIncreaseContext
import at.posselt.pfrpg2e.kingdom.data.RawRuinThresholdIncreasesContext
import at.posselt.pfrpg2e.kingdom.formatRequirements
import at.posselt.pfrpg2e.kingdom.satisfiesRequirements
import at.posselt.pfrpg2e.utils.t
import js.objects.JsPlainObject

@Suppress("unused")
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

@Suppress("unused")
@JsPlainObject
external interface FeatureContext {
    val id: FormElementContext
    val name: String
    val description: String
    val automationNotes: String?
    val featRequirements: String?
    val abilityBoosts: AbilityBoostContext?
    val feat: FormElementContext?
    val featDescription: String?
    val featAutomationNotes: String?
    val featSatisfiesRequirements: Boolean
    val skillProficiency: FormElementContext?
    val ruinThresholdIncreases: RuinThresholdIncreases?
    val featRuinThresholdIncreases: Array<RuinThresholdIncreases>
    val removeLeaderVacancyPenalty: FormElementContext?
}

fun RawRuinThresholdIncreaseContext.toContext(
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

fun RawRuinThresholdIncreasesContext.toContext(prefix: String, amount: Int): RuinThresholdIncreases =
    RuinThresholdIncreases(
        value = crime.value,
        amount = amount,
        crime = crime.toContext("$prefix.crime", "Crime"),
        corruption = corruption.toContext("$prefix.corruption", "Corruption"),
        strife = strife.toContext("$prefix.strife", "Strife"),
        stability = decay.toContext("$prefix.decay", "Decay"),
    )


fun defaultRuinThresholdIncrease(value: Int) =
    RawRuinThresholdIncreasesContext(
        crime = RawRuinThresholdIncreaseContext(
            value = value,
            increase = false,
        ),
        corruption = RawRuinThresholdIncreaseContext(
            value = value,
            increase = false,
        ),
        strife = RawRuinThresholdIncreaseContext(
            value = value,
            increase = false,
        ),
        decay = RawRuinThresholdIncreaseContext(
            value = value,
            increase = false,
        ),
    )

private fun RawGovernment.getFeatIds() =
    setOf(bonusFeat) + skillProficiencies.map { "skill-training-$it" }

fun getTakenFeats(
    choices: Array<RawFeatureChoices>,
    government: RawGovernment?,
    bonusFeats: Array<RawBonusFeat>,
    trainedSkills: Set<KingdomSkill>,
): Set<String> =
    choices.mapNotNull { it.featId }.toSet() +
            government?.getFeatIds().orEmpty() +
            bonusFeats.map { it.id } +
            trainedSkills.map { "skill-training-${it.value}" }.toSet()

fun Array<RawFeatureChoices>.toContext(
    government: RawGovernment?,
    features: Array<RawExplodedKingdomFeature>,
    feats: Array<RawFeat>,
    increaseBoostsBy: Int,
    navigationEntry: String,
    bonusFeats: Array<RawBonusFeat>,
    trainedSkills: Set<KingdomSkill>,
    chosenFeats: List<ChosenFeat>,
    abilityScores: KingdomAbilityScores,
    skillRanks: KingdomSkillRanks,
): Array<FeatureByLevelContext> {
    val choices = this
    val chosenFeatIds = chosenFeats.map { it.feat.id }.toSet()
    val featsById = feats.associateBy { it.id }
    val choicesById = choices.associateBy { it.id }
    val takenFeats = getTakenFeats(choices, government, bonusFeats, trainedSkills)
    return features
        .asSequence()
        .mapIndexed { index, feature -> index to feature }
        .groupBy { (_, feature) -> feature.level }
        .map { (level, f) ->
            val highestProficiency = highestProficiencyByLevel[level] ?: Proficiency.UNTRAINED
            FeatureByLevelContext(
                level = level,
                hidden = navigationEntry != "$level",
                features = f.map { (index, feature) ->
                    val choice = choicesById[feature.id]
                    val feat = choice?.featId?.let { featsById[it] }
                    val ruinThresholdIncreases = feature.ruinThresholdIncreases
                    val abilityBoosts = feature.abilityBoosts
                    FeatureContext(
                        id = HiddenInput(name = "features.$index.id", value = feature.id).toContext(),
                        name = feature.name,
                        description = if (feature.skillIncrease == true) {
                            feature.description + ". You can increase a skill up to ${t(highestProficiency)}."
                        } else {
                            feature.description
                        },
                        automationNotes = feature.automationNotes,
                        featSatisfiesRequirements = feat?.satisfiesRequirements(
                            chosenFeatIds = chosenFeatIds,
                            skillRanks = skillRanks,
                            abilityScores = abilityScores,
                        ) == true,
                        removeLeaderVacancyPenalty = if (feat?.removeLeaderVacancyPenalty == true) {
                            Select.fromEnum<Leader>(
                                label = "Supported Leader",
                                name = "features.$index.supportedLeader",
                                required = false,
                                stacked = false,
                                value = choice.supportedLeader?.let { Leader.fromString(it) }
                            ).toContext()
                        } else {
                            null
                        },
                        featAutomationNotes = feat?.automationNotes,
                        featRequirements = feat?.requirements?.formatRequirements(feats),
                        abilityBoosts = if (abilityBoosts != null) {
                            val free = abilityBoosts + increaseBoostsBy
                            val boosts = choice?.abilityBoosts ?: RawAbilityBoostChoices(
                                culture = false,
                                economy = false,
                                loyalty = false,
                                stability = false,
                            )
                            boosts.toContext("features.$index.", free)
                        } else {
                            null
                        },
                        feat = if (feature.kingdomFeat == true) {
                            val featSelectOptions = feats
                                .filter { it.level <= level && (it.id == feat?.id || it.id !in takenFeats) }
                                .map { SelectOption(it.name, it.id) }
                            Select(
                                label = "Kingdom Feat",
                                name = "features.$index.featId",
                                value = feat?.id,
                                options = featSelectOptions,
                                required = false,
                                stacked = false,
                                hideLabel = true,
                            ).toContext()
                        } else {
                            null
                        },

                        featDescription = feat?.text ?: "",
                        skillProficiency = if (feature.skillIncrease == true) {
                            Select.fromEnum<KingdomSkill>(
                                label = "Skill Increase",
                                name = "features.$index.skillIncrease",
                                value = choice?.skillIncrease?.let { KingdomSkill.fromString(it) },
                                required = false,
                                stacked = false,
                                hideLabel = true,
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
                            val choice: RuinThresholdIncreases? =
                                choice.featRuinThresholdIncreases.getOrNull(ruinIndex)
                                    ?.toContext("features.$index.featRuinThresholdIncreases.$ruinIndex", value.amount)
                            val increase: RuinThresholdIncreases = defaultRuinThresholdIncrease(value.increase)
                                .toContext("features.$index.featRuinThresholdIncreases.$ruinIndex", value.amount)
                            choice ?: increase
                        }?.toTypedArray() ?: emptyArray(),
                    )
                }
                    .sortedBy { it.name }
                    .toTypedArray()
            )
        }.toTypedArray()

}
