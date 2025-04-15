package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.data.kingdom.KingdomAbility
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.kingdom.RawFeat
import at.posselt.pfrpg2e.kingdom.RawGovernment
import at.posselt.pfrpg2e.kingdom.data.RawGovernmentChoices
import at.posselt.pfrpg2e.kingdom.formatRequirements
import at.posselt.pfrpg2e.utils.t
import js.objects.JsPlainObject

@Suppress("unused")
@JsPlainObject
external interface GovernmentContext {
    val type: FormElementContext
    val description: String?
    val abilityBoosts: AbilityBoostContext
    val skills: String
    val feat: String
    val boosts: String
    val featDescription: String
    val featAutomationNotes: String?
    val featRequirements: String?
    val removeLeaderVacancyPenalty: FormElementContext?
    val featRuinThresholdIncreases: Array<RuinThresholdIncreases>
}

fun RawGovernmentChoices.toContext(
    governments: List<RawGovernment>,
    feats: Array<RawFeat>,
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
        featRuinThresholdIncreases = feat?.ruinThresholdIncreases?.mapIndexed { ruinIndex, value ->
            val choice: RuinThresholdIncreases? =
                featRuinThresholdIncreases?.getOrNull(ruinIndex)
                    ?.toContext("government.featRuinThresholdIncreases.$ruinIndex", value.amount)
            val increase: RuinThresholdIncreases = defaultRuinThresholdIncrease(value.increase)
                .toContext("government.featRuinThresholdIncreases.$ruinIndex", value.amount)
            choice ?: increase
        }?.toTypedArray() ?: emptyArray(),
        removeLeaderVacancyPenalty = if (feat?.removeLeaderVacancyPenalty == true) {
            Select.fromEnum<Leader>(
                label = "Supported Leader",
                name = "government.featSupportedLeader",
                required = false,
                stacked = false,
                value = featSupportedLeader?.let { Leader.fromString(it) }
            ).toContext()
        } else {
            null
        },
        boosts = governmentBoosts
            ?.mapNotNull { KingdomAbility.fromString(it) }
            ?.joinToString(", ") { t(it) } ?: "",
        description = government?.description,
        skills = government?.skillProficiencies
            ?.mapNotNull { KingdomSkill.fromString(it) }
            ?.joinToString(", ") { t(it) } ?: "",
        feat = feat?.name ?: "",
        featDescription = feat?.text ?: "",
        featAutomationNotes = feat?.automationNotes,
        featRequirements = feat?.requirements?.formatRequirements(feats),
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