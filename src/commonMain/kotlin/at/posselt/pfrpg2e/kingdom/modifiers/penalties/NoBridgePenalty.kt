package at.posselt.pfrpg2e.kingdom.modifiers.penalties

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Eq

fun noBridgePenalty(settlements: List<Settlement>): Modifier? {
    val settlementsLackingBridge = settlements.filter { it.lacksBridge }
    return if (settlementsLackingBridge.isNotEmpty()) {
        Modifier(
            id = "lacking-bridge",
            name = "Lacking Bridges (${settlementsLackingBridge.joinToString(", ") { it.name }})",
            value = -settlementsLackingBridge.size,
            applyIf = listOf(
                Eq("@skill", KingdomSkill.TRADE.value)
            ),
            type = ModifierType.ITEM,
        )
    } else {
        null
    }
}
