package at.posselt.pfrpg2e.kingdom.unrest

import at.posselt.pfrpg2e.data.kingdom.leaders.Vacancies
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement

fun calculateUnrest(
    atWar: Boolean,
    settlements: List<Settlement>,
    vacancies: Vacancies,
) = GainedUnrest(
    war = if (atWar) 1 else 0,
    overcrowded = settlements.count { it.isOvercrowded },
    secondaryTerritory = if(settlements.any { it.isSecondaryTerritory }) 1 else 0,
    rulerVacant = vacancies.ruler
)