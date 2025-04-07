package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.data.RawAbilityBoostChoices
import at.posselt.pfrpg2e.kingdom.data.RawRuinValues
import at.posselt.pfrpg2e.kingdom.getGovernments
import at.posselt.pfrpg2e.kingdom.getMilestones
import at.posselt.pfrpg2e.utils.postChatTemplate
import com.foundryvtt.core.ui.enrichHtml
import js.objects.recordOf

suspend fun beforeKingdomUpdate(previous: KingdomData, current: KingdomData) {
    // when choosing a new kingdom feat when the previous one had threshold increases,
    // the old checkbox are still in the dom once the new feat choice is being submitted
    // causing the new feature choice to gain the old boosts so we need to clear those on
    // each feat change
    val previousChosenKingdomFeatsById = previous.features
        .filter { it.id.startsWith("kingdom-feat") }
        .associateBy { it.id }
    val chosenKingdomFeatsById = current.features.filter { it.id.startsWith("kingdom-feat") }
    chosenKingdomFeatsById.forEach { feat ->
        if (previousChosenKingdomFeatsById[feat.id]?.featId != feat.featId) {
            feat.featRuinThresholdIncreases = emptyArray()
            feat.supportedLeader = null
        }
    }

    if (previous.settings.kingdomSkillIncreaseEveryLevel != current.settings.kingdomSkillIncreaseEveryLevel) {
        val additionalSkillIncreases = (2..20 step 2)
            .map { "skill-increase-level-$it" }
            .toSet()
        current.features = current.features.filter { it.id !in additionalSkillIncreases }.toTypedArray()
    }

    val charterType = current.charter.type
    if (previous.charter.type != charterType) {
        resetAbilityBoosts(current.charter.abilityBoosts)
    }
    val governmentType = current.government.type
    if (previous.government.type != governmentType) {
        val government = previous.getGovernments().find { it.id == governmentType }
        resetAbilityBoosts(current.government.abilityBoosts)
        current.government.featSupportedLeader = null
        current.government.featRuinThresholdIncreases = null
        if (government != null) {
            val governmentFeats = government.skillProficiencies
                .map { "skill-training-$it" }
                .toSet() + government.bonusFeat
            current.features
                .filter { it.featId in governmentFeats }
                .forEach { it.featId = null }
            current.bonusFeats = current.bonusFeats
                .filterNot { it.id in governmentFeats }
                .toTypedArray()
        }
    }
    val xp = calculateMilestoneXp(current.getMilestones(), previous.milestones, current.milestones)
    val change = current.calculateXpChange(xp)
    change.toChat()
    current.xp += change.addXp
    current.level += change.addLevel
    checkRuin(previous.ruin.corruption, current.ruin.corruption, "Corruption")
    checkRuin(previous.ruin.crime, current.ruin.crime, "Crime")
    checkRuin(previous.ruin.decay, current.ruin.decay, "Decay")
    checkRuin(previous.ruin.strife, current.ruin.strife, "Strife")
}

private suspend fun checkRuin(
    previous: RawRuinValues,
    current: RawRuinValues,
    label: String,
) {
    if (previous.value != current.value && current.penalty > 0 && current.value == 0) {
        val check = enrichHtml("@Check[type:flat|dc:16]")
        postChatTemplate(
            templatePath = "chatmessages/reduce-ruin-penalty.hbs",
            templateContext = recordOf(
                "ruin" to label,
                "check" to check
            )
        )
    }
}

fun resetAbilityBoosts(abilityBoosts: RawAbilityBoostChoices) {
    abilityBoosts.economy = false
    abilityBoosts.loyalty = false
    abilityBoosts.culture = false
    abilityBoosts.stability = false
}