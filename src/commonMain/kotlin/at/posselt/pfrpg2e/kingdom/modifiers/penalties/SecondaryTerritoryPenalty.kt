package at.posselt.pfrpg2e.kingdom.modifiers.penalties

import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType

fun secondaryTerritoryPenalty(activeSettlement: Settlement): Modifier? =
    if(activeSettlement.isSecondaryTerritory) {
        Modifier(
            type = ModifierType.CIRCUMSTANCE,
            value = -4,
            id = "secondary-territory",
            name = "Check in Secondary Territory",
        )
    } else {
        null
    }