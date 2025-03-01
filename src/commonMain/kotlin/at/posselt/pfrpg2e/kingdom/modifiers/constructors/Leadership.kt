package at.posselt.pfrpg2e.kingdom.modifiers.constructors

import at.posselt.pfrpg2e.data.actor.Attribute
import at.posselt.pfrpg2e.data.actor.Lore
import at.posselt.pfrpg2e.data.actor.Skill
import at.posselt.pfrpg2e.data.actor.SkillRanks
import at.posselt.pfrpg2e.data.kingdom.Leader
import at.posselt.pfrpg2e.data.kingdom.LeaderSkillRanks
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType.LEADERSHIP
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.EqPredicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.InPredicate

private fun calculateLeadershipBonus(
    leaderSkills: List<Attribute>,
    leaderSkillRanks: SkillRanks,
): Int {
    val highestLoreRank = leaderSkills
        .asSequence()
        .filterIsInstance<Lore>()
        .map { leaderSkillRanks.resolveRank(it) }
        .maxOrNull() ?: 0
    val skillRanks = leaderSkills
        .asSequence()
        .filterIsInstance<Skill>()
        .map { leaderSkillRanks.resolveRank(it) }
    val applicableRanks = skillRanks + highestLoreRank
    return if (applicableRanks.count { it >= 4 } >= 2) {
        4
    } else if (applicableRanks.count { it >= 3 } >= 2) {
        3
    } else if (applicableRanks.count { it >= 2 } >= 2) {
        2
    } else if (applicableRanks.count { it >= 1 } >= 2) {
        1
    } else {
        0
    }
}

fun createLeadershipModifiers(
    leaderSkills: LeaderSkills,
    leaderSkillRanks: LeaderSkillRanks,
    leaderKingdomSkills: LeaderKingdomSkills,
): List<Modifier> {
    return Leader.entries.flatMap { leader ->
        val value = calculateLeadershipBonus(
            leaderSkills.resolveAttributes(leader),
            leaderSkillRanks.resolveRanks(leader),
        )
        val fullModifier = Modifier(
            id = leader.value,
            type = LEADERSHIP,
            value = value,
            name = "Leadership (Specialized)",
            predicates = listOf(
                EqPredicate("@leader", leader.value),
                InPredicate(
                    "@skill", leaderKingdomSkills.resolveAttributes(leader)
                        .map { it.value }
                        .toSet())
            ),
        )
        listOf(
            fullModifier,
            fullModifier.copy(
                value = fullModifier.value / 2,
                name = "Leadership (Unspecialized)",
                predicates = listOf(EqPredicate("@leader", leader.value)),
            )
        )
    }
}