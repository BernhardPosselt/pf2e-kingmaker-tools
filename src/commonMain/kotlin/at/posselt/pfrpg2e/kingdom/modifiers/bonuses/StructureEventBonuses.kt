package at.posselt.pfrpg2e.kingdom.modifiers.bonuses

import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.EqPredicate

fun structureEventBonuses(settlement: Settlement): Modifier? =
    if (settlement.settlementEventBonus > 0) {
        Modifier(
            type = ModifierType.ITEM,
            name = "Structure Event Bonus",
            value = settlement.settlementEventBonus,
            predicates = listOf(
                EqPredicate("@phase", KingdomPhase.EVENT.value)
            ),
            id = "structure-event-bonus"
        )
    } else {
        null
    }