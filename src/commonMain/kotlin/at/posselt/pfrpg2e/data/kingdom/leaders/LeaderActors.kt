package at.posselt.pfrpg2e.data.kingdom.leaders

import at.posselt.pfrpg2e.data.actor.SkillRanks

data class LeaderActor(
    val level: Int,
    val type: LeaderType,
    val ranks: SkillRanks,
    val invested: Boolean,
    val uuid: String,
    val img: String?,
    val name: String,
)

data class LeaderActors (
    val ruler: LeaderActor? = null,
    val counselor: LeaderActor? = null,
    val emissary: LeaderActor? = null,
    val general: LeaderActor? = null,
    val magister: LeaderActor? = null,
    val treasurer: LeaderActor? = null,
    val viceroy: LeaderActor? = null,
    val warden: LeaderActor? = null,
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