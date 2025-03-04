package at.posselt.pfrpg2e.kingdom.modifiers.bonuses

import at.posselt.pfrpg2e.data.actor.Attribute
import at.posselt.pfrpg2e.data.actor.Lore
import at.posselt.pfrpg2e.data.actor.Skill
import at.posselt.pfrpg2e.data.actor.SkillRanks
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderActors
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderKingdomSkills
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderSkills
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderType
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType.LEADERSHIP
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Eq
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.In


private fun calculateLeadershipBonus(
    leaderLevel: Int,
    leaderType: LeaderType,
    leaderSkills: List<Attribute>,
    leaderSkillRanks: SkillRanks,
) = when (leaderType) {
    LeaderType.PC -> calculatePcBonus(leaderSkills, leaderSkillRanks)
    LeaderType.REGULAR_NPC -> calculateRegularNpcBonus(leaderLevel)
    LeaderType.HIGHLY_MOTIVATED_NPC -> calculateHighlyMotivatedNpcBonus(leaderLevel)
    LeaderType.NON_PATHFINDER_NPC -> calculateNonPathfinderNpcBonus(leaderLevel)
}

private fun calculateHighlyMotivatedNpcBonus(level: Int) =
    if (level >= 1 && level <= 3) {
        1
    } else if (level >= 4 && level <= 7) {
        2
    } else if (level >= 8 && level <= 15) {
        3
    } else {
        4
    }

private fun calculateRegularNpcBonus(level: Int) =
    if (level >= 1 && level <= 5) {
        1
    } else if (level >= 6 && level <= 9) {
        2
    } else {
        3
    }

private fun calculateNonPathfinderNpcBonus(level: Int) =
    if (level >= 1 && level <= 4) {
        1
    } else if (level >= 5 && level <= 8) {
        2
    } else if (level >= 9 && level <= 16) {
        3
    } else {
        4
    }

private fun calculatePcBonus(
    leaderSkills: List<Attribute>,
    leaderSkillRanks: SkillRanks
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
    leaderActors: LeaderActors,
    leaderSkills: LeaderSkills,
    leaderKingdomSkills: LeaderKingdomSkills,
): List<Modifier> {
    return Leader.entries.flatMap { leader ->
        val actor = leaderActors.resolve(leader)
        if (actor == null) {
            emptyList()
        } else {
        val value = calculateLeadershipBonus(
            actor.level,
            actor.type,
            leaderSkills.resolveAttributes(leader),
            actor.ranks,
        )
        val fullModifier = Modifier(
            id = leader.value,
            type = LEADERSHIP,
            value = value,
            name = "Leadership (Specialized)",
            applyIf = listOf(
                Eq("@leader", leader.value),
                In(
                    "@skill", leaderKingdomSkills.resolveAttributes(leader)
                        .map { it.value }
                        .toList())
            ),
        )
        listOf(
            fullModifier,
            fullModifier.copy(
                value = fullModifier.value / 2,
                name = "Leadership (Unspecialized)",
                applyIf = listOf(Eq("@leader", leader.value)),
            )
        )
        }
    }
}