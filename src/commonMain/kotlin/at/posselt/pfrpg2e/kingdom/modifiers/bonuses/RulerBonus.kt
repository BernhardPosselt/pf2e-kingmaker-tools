package at.posselt.pfrpg2e.kingdom.modifiers.bonuses

import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.data.kingdom.structures.EvaluatedStructureBonuses
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.EqPredicate

fun rulerBonus(leader: Leader, structures: EvaluatedStructureBonuses): Modifier? =
    if (leader == Leader.RULER && structures.leaderBonus > 0) {
        Modifier(
            type = ModifierType.ITEM,
            name = "Ruler Performs Leadership Activity",
            value = structures.leaderBonus,
            predicates = listOf(
                EqPredicate("@phase", KingdomPhase.LEADERSHIP.value)
            ),
            id = "ruler-bonus"
        )
    } else {
        null
    }