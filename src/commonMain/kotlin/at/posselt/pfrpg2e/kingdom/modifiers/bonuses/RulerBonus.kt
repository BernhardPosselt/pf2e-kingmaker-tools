package at.posselt.pfrpg2e.kingdom.modifiers.bonuses

import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.GlobalStructureBonuses
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Eq

fun createRulerBonus(global: GlobalStructureBonuses): Modifier? =
    if (global.leaderLeadershipActivityBonus > 0) {
        Modifier(
            type = ModifierType.ITEM,
            name = "Ruler Performs Leadership Activity",
            value = global.leaderLeadershipActivityBonus,
            applyIf = listOf(
                Eq("@leader", Leader.RULER.value),
                Eq("@phase", KingdomPhase.LEADERSHIP.value),
            ),
            id = "ruler-bonus"
        )
    } else {
        null
    }