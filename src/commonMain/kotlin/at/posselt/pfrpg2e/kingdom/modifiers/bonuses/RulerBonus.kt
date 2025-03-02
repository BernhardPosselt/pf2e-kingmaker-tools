package at.posselt.pfrpg2e.kingdom.modifiers.bonuses

import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.EqPredicate

fun rulerBonus(leader: Leader, settlement: Settlement): Modifier? =
    if (leader == Leader.RULER && settlement.leaderLeadershipActivityBonus > 0) {
        Modifier(
            type = ModifierType.ITEM,
            name = "Ruler Performs Leadership Activity",
            value = settlement.leaderLeadershipActivityBonus,
            predicates = listOf(
                EqPredicate("@phase", KingdomPhase.LEADERSHIP.value)
            ),
            id = "ruler-bonus"
        )
    } else {
        null
    }