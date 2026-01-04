package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.data.kingdom.KingdomAbilityScores
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRanks
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.kingdom.RawFeat
import at.posselt.pfrpg2e.kingdom.RawGovernment
import at.posselt.pfrpg2e.kingdom.RawRuinThresholdIncreases
import at.posselt.pfrpg2e.kingdom.data.ChosenFeat
import at.posselt.pfrpg2e.kingdom.data.RawBonusFeat
import at.posselt.pfrpg2e.kingdom.data.RawFeatureChoices
import at.posselt.pfrpg2e.kingdom.formatRequirements
import at.posselt.pfrpg2e.kingdom.satisfiesRequirements
import at.posselt.pfrpg2e.utils.t
import js.objects.JsPlainObject

@JsPlainObject
external interface AddBonusFeatContext {
    val feat: FormElementContext
    val description: String
    val name: String
    val automationNotes: String?
    val requirements: String?
    val satisfiesRequirements: Boolean
}

@Suppress("unused")
@JsPlainObject
external interface BonusFeatContext {
    val id: FormElementContext
    val idValue: String
    val name: String
    val description: String
    val automationNotes: String?
    val ruinThresholdIncreases: Array<RuinThresholdIncreases>
    val requirements: String?
    val satisfiesRequirements: Boolean
    val removeLeaderVacancyPenalty: FormElementContext?
}

fun createBonusFeatContext(
    government: RawGovernment?,
    feats: Array<RawFeat>,
    choices: Array<RawFeatureChoices>,
    bonusFeats: Array<RawBonusFeat>,
    value: String?,
    trainedSkills: Set<KingdomSkill>,
    chosenFeats: List<ChosenFeat>,
    abilityScores: KingdomAbilityScores,
    skillRanks: KingdomSkillRanks,
): AddBonusFeatContext {
    val takenFeats = getTakenFeats(choices, government, bonusFeats, trainedSkills)
    val feat = feats.find { it.id == value }
    val chosenFeatIds = chosenFeats.map { it.feat.id }.toSet()
    return AddBonusFeatContext(
        feat = Select(
            name = "bonusFeat",
            label = t("kingdom.feat"),
            value = value,
            options = feats
                .filter { it.id == value || it.id !in takenFeats }
                .map { SelectOption(it.name, it.id) },
            required = false,
            stacked = false,
            hideLabel = true,
        ).toContext(),
        description = feat?.text ?: "",
        satisfiesRequirements = feat?.satisfiesRequirements(
            chosenFeatIds = chosenFeatIds,
            skillRanks = skillRanks,
            abilityScores = abilityScores,
        ) == true,
        requirements = feat?.requirements?.formatRequirements(feats),
        automationNotes = feat?.automationNotes,
        name = feat?.name ?: "",
    )
}

fun Array<RawBonusFeat>.toContext(
    feats: Array<RawFeat>,
    chosenFeats: List<ChosenFeat>,
    abilityScores: KingdomAbilityScores,
    skillRanks: KingdomSkillRanks,
): Array<BonusFeatContext> {
    val featsById = feats.associateBy { it.id }
    val chosenFeatIds = chosenFeats.map { it.feat.id }.toSet()
    return mapIndexedNotNull { index, rawFeat ->
        featsById[rawFeat.id]?.let { feat ->
            val increases: Array<RawRuinThresholdIncreases> = feat.ruinThresholdIncreases ?: emptyArray()
            BonusFeatContext(
                id = HiddenInput(name = "bonusFeats.$index.id", value = feat.id).toContext(),
                idValue = feat.id,
                name = feat.name,
                description = feat.text,
                satisfiesRequirements = feat.satisfiesRequirements(
                    chosenFeatIds = chosenFeatIds,
                    skillRanks = skillRanks,
                    abilityScores = abilityScores,
                ) == true,
                removeLeaderVacancyPenalty = if (feat.removeLeaderVacancyPenalty == true) {
                    Select.fromEnum<Leader>(
                        label = t("kingdom.supportedLeader"),
                        name = "bonusFeats.$index.supportedLeader",
                        required = false,
                        stacked = false,
                        value = rawFeat.supportedLeader?.let { Leader.fromString(it) }
                    ).toContext()
                } else {
                    null
                },
                automationNotes = feat.automationNotes,
                requirements = feat.requirements?.formatRequirements(feats),
                ruinThresholdIncreases = increases.mapIndexed { ruinIndex, value ->
                    val choice = rawFeat.ruinThresholdIncreases.getOrNull(ruinIndex)
                        ?.toContext("bonusFeats.$index.ruinThresholdIncreases.$ruinIndex", value.amount)
                    val default = defaultRuinThresholdIncrease(value.increase)
                        .toContext("bonusFeats.$index.ruinThresholdIncreases.$ruinIndex", value.amount)
                    choice ?: default
                }.toTypedArray(),
            )
        }
    }.toTypedArray()
}