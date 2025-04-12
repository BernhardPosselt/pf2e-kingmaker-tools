package at.posselt.pfrpg2e.kingdom.modifiers.bonuses

import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderActors
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Eq

fun calculateInvestedBonus(
    kingdomLevel: Int,
    leaderActors: LeaderActors,
): LeaderBonuses {
    val value = if (kingdomLevel < 8) {
        1
    } else if (kingdomLevel < 16) {
        2
    } else {
        3
    }
    return LeaderBonuses(
        ruler = if (leaderActors.resolve(Leader.RULER)?.invested == true) value else 0,
        counselor = if (leaderActors.resolve(Leader.COUNSELOR)?.invested == true) value else 0,
        emissary = if (leaderActors.resolve(Leader.EMISSARY)?.invested == true) value else 0,
        general = if (leaderActors.resolve(Leader.GENERAL)?.invested == true) value else 0,
        magister = if (leaderActors.resolve(Leader.MAGISTER)?.invested == true) value else 0,
        treasurer = if (leaderActors.resolve(Leader.TREASURER)?.invested == true) value else 0,
        viceroy = if (leaderActors.resolve(Leader.VICEROY)?.invested == true) value else 0,
        warden = if (leaderActors.resolve(Leader.WARDEN)?.invested == true) value else 0,
    )
}

fun createInvestedBonuses(
    kingdomLevel: Int,
    leaderActors: LeaderActors,
): List<Modifier> {
    val bonuses = calculateInvestedBonus(kingdomLevel, leaderActors)
    return Leader.entries
        .mapNotNull {
            val value = bonuses.resolve(it)
            if (value > 0) {
                val ability = it.keyAbility.value
                Modifier(
                    type = ModifierType.STATUS,
                    value = value,
                    id = "invested-$ability",
                    name = "builtInModifierNames.invested",
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