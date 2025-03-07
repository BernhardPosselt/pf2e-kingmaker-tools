package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderActors
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderSkills
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.calculateLeadershipBonus

fun getHighestLeadershipModifiers(
    leaderActors: LeaderActors,
    leaderSkills: LeaderSkills,
) = Leader.entries.associate { leader ->
    val bonus = leaderActors.resolve(leader)?.let { actor ->
        calculateLeadershipBonus(
            leaderLevel = actor.level,
            leaderType = actor.type,
            leaderSkills = leaderSkills.resolveAttributes(leader),
            leaderSkillRanks = actor.ranks,
        )
    } ?: 0
    leader to bonus
}
