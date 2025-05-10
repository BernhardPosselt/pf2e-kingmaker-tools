package at.posselt.pfrpg2e.kingdom.modifiers.bonuses

import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderActors
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderSkills

data class LeaderBonuses (
    val ruler: Int = 0,
    val counselor: Int = 0,
    val emissary: Int = 0,
    val general: Int = 0,
    val magister: Int = 0,
    val treasurer: Int = 0,
    val viceroy: Int = 0,
    val warden: Int = 0,
) {
    fun resolve(leader: Leader) =
        when (leader) {
            Leader.RULER -> ruler
            Leader.COUNSELOR -> counselor
            Leader.EMISSARY -> emissary
            Leader.GENERAL -> general
            Leader.MAGISTER -> magister
            Leader.TREASURER -> treasurer
            Leader.VICEROY -> viceroy
            Leader.WARDEN -> warden
        }
}

fun getHighestLeadershipModifiers(
    leaderActors: LeaderActors,
    leaderSkills: LeaderSkills,
): LeaderBonuses {
    val bonuses = Leader.entries.associateWith { leader ->
        leaderActors.resolve(leader)?.let { actor ->
            calculateLeadershipBonus(
                leaderLevel = actor.level,
                leaderType = actor.type,
                leaderSkills = leaderSkills.resolveAttributes(leader),
                leaderSkillRanks = actor.ranks,
            )
        } ?: 0
    }
    return LeaderBonuses(
        ruler = bonuses[Leader.RULER] ?: 0,
        counselor = bonuses[Leader.COUNSELOR] ?: 0,
        emissary = bonuses[Leader.EMISSARY] ?: 0,
        general = bonuses[Leader.GENERAL] ?: 0,
        magister = bonuses[Leader.MAGISTER] ?: 0,
        treasurer = bonuses[Leader.TREASURER] ?: 0,
        viceroy = bonuses[Leader.VICEROY] ?: 0,
        warden = bonuses[Leader.WARDEN] ?: 0
    )
}
