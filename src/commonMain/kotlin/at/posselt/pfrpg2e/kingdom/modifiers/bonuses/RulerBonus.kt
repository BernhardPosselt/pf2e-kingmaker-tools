package at.posselt.pfrpg2e.kingdom.modifiers.bonuses

import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.GlobalStructureBonuses
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.EqPredicate

fun createRulerBonus(global: GlobalStructureBonuses): Modifier? =
    if (global.leaderLeadershipActivityBonus > 0) {
        Modifier(
            type = ModifierType.ITEM,
            name = "Ruler Performs Leadership Activity",
            value = global.leaderLeadershipActivityBonus,
            predicates = listOf(
                EqPredicate("@leader", Leader.RULER.value),
                EqPredicate("@phase", KingdomPhase.LEADERSHIP.value),
            ),
            id = "ruler-bonus"
        )
    } else {
        null
    }