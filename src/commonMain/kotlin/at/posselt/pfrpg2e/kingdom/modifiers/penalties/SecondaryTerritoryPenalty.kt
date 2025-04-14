package at.posselt.pfrpg2e.kingdom.modifiers.penalties

import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType

fun createSecondaryTerritoryPenalty(currentSettlement: Settlement): Modifier? =
    if(currentSettlement.isSecondaryTerritory) {
        Modifier(
            type = ModifierType.CIRCUMSTANCE,
            value = -4,
            id = "secondary-territory",
            name = "modifiers.penalties.secondaryTerritory",
        )
    } else {
        null
    }