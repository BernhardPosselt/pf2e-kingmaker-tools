package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.data.RawAbilityBoostChoices
import at.posselt.pfrpg2e.kingdom.getGovernments

fun beforeKingdomUpdate(previous: KingdomData, current: KingdomData) {
    val charterType = current.charter.type
    if (previous.charter.type != charterType) {
        resetAbilityBoosts(current.charter.abilityBoosts)
    }
    val governmentType = current.government.type
    if (previous.government.type != governmentType) {
        val government = previous.getGovernments().find { it.id == governmentType }
        resetAbilityBoosts(current.government.abilityBoosts)
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
}

fun resetAbilityBoosts(abilityBoosts: RawAbilityBoostChoices) {
    abilityBoosts.economy = false
    abilityBoosts.loyalty = false
    abilityBoosts.culture = false
    abilityBoosts.stability = false
}