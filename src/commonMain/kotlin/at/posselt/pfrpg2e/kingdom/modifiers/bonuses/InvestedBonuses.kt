package at.posselt.pfrpg2e.kingdom.modifiers.bonuses

import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderActors
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Eq

fun createInvestedBonuses(
    kingdomLevel: Int,
    leaderActors: LeaderActors,
): List<Modifier> {
    val value = if (kingdomLevel < 8) {
        1
    } else if (kingdomLevel < 16) {
        2
    } else {
        3
    }
    return Leader.entries
        .mapNotNull {
            if (leaderActors.resolve(it)?.invested == true) {
                val ability = it.keyAbility.value
                Modifier(
                    type = ModifierType.STATUS,
                    value = value,
                    id = "invested-$ability",
                    name = "Invested (${ability})",
                    applyIf = listOf(
                        Eq("@ability", ability)
                    )
                )
            } else {
                null
            }
        }
        .distinctBy { it.id }
}