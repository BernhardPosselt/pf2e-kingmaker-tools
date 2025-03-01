package at.posselt.pfrpg2e.data.kingdom

import at.posselt.pfrpg2e.data.actor.SkillRanks

data class LeaderSkillRanks(
    val ruler: SkillRanks = SkillRanks(),
    val counselor: SkillRanks = SkillRanks(),
    val emissary: SkillRanks = SkillRanks(),
    val general: SkillRanks = SkillRanks(),
    val magister: SkillRanks = SkillRanks(),
    val treasurer: SkillRanks = SkillRanks(),
    val viceroy: SkillRanks = SkillRanks(),
    val warden: SkillRanks = SkillRanks(),
) {
    fun resolveRanks(leader: Leader) =
        when(leader) {
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
