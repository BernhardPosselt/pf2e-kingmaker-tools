package at.posselt.pfrpg2e.kingdom.modifiers.bonuses

import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Eq

fun createStructureEventBonuses(currentSettlement: Settlement): Modifier? =
    if (currentSettlement.settlementEventBonus > 0) {
        Modifier(
            type = ModifierType.ITEM,
            name = "modifiers.bonuses.structureEventBonus",
            value = currentSettlement.settlementEventBonus,
            applyIf = listOf(
                Eq("@phase", KingdomPhase.EVENT.value)
            ),
            id = "structure-event-bonus"
        )
    } else {
        null
    }