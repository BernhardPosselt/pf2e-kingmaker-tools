package at.posselt.pfrpg2e.kingdom.modifiers.bonuses

import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Eq

fun createLeaderEventBonus(
    kingdomLevel: Int,
): Modifier {
    val value = if (kingdomLevel >= 15) {
        3
    } else if (kingdomLevel >= 9) {
        2
    } else {
        1
    }
    return Modifier(
        id = "leader-event-bonus",
        name = "modifiers.bonuses.listedEventLeader",
        value = value,
        applyIf = listOf(
            Eq("@phase", KingdomPhase.EVENT.value),
            Eq("@leaderVacant", false),
            Eq("@eventLeader", "@leader")
        ),
        type = ModifierType.CIRCUMSTANCE,
    )
}