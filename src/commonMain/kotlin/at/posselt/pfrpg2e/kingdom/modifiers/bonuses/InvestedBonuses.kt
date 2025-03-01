package at.posselt.pfrpg2e.kingdom.modifiers.bonuses

import at.posselt.pfrpg2e.data.kingdom.leaders.InvestedLeaders
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.EqPredicate

fun createInvestedBonuses(
    kingdomLevel: Int,
    investedLeaders: InvestedLeaders,
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
            if (investedLeaders.isInvested(it)) {
                val ability = it.keyAbility.value
                Modifier(
                    type = ModifierType.STATUS,
                    value = value,
                    id = "invested-$ability",
                    name = "Invested (${ability})",
                    predicates = listOf(
                        EqPredicate("@ability", ability)
                    )
                )
            } else {
                null
            }
        }
        .distinctBy { it.id }
}