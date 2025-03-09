package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.data.RawAbilityBoostChoices
import at.posselt.pfrpg2e.kingdom.getGovernments

fun beforeKingdomUpdate(previous: KingdomData, current: KingdomData) {
    val charterType = current.charter.type
    if (charterType != null && previous.charter.type != charterType) {
        resetAbilityBoosts(current.charter.abilityBoosts)
    }
    val governmentType = current.government.type
    if (governmentType != null && previous.government.type != governmentType) {
        val government = previous.getGovernments().find { it.id == governmentType }
        resetAbilityBoosts(current.government.abilityBoosts)
        if (government != null) {
            current.features
                .find { it.featId == government.bonusFeat }
                ?.let { it.featId = null }
            // TODO: filter skill training based off government skill proficiencies
            current.bonusFeats = current.bonusFeats.filter { it.id != government.bonusFeat }.toTypedArray()
        }
    }
}

private fun resetAbilityBoosts(abilityBoosts: RawAbilityBoostChoices) {
    abilityBoosts.economy = false
    abilityBoosts.loyalty = false
    abilityBoosts.culture = false
    abilityBoosts.stability = false
}