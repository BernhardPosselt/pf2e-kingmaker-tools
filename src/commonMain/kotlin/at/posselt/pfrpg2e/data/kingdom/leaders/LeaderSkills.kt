package at.posselt.pfrpg2e.data.kingdom.leaders

import at.posselt.pfrpg2e.data.actor.Attribute

data class LeaderSkills(
    val ruler: List<Attribute> = emptyList(),
    val counselor: List<Attribute> = emptyList(),
    val emissary: List<Attribute> = emptyList(),
    val general: List<Attribute> = emptyList(),
    val magister: List<Attribute> = emptyList(),
    val treasurer: List<Attribute> = emptyList(),
    val viceroy: List<Attribute> = emptyList(),
    val warden: List<Attribute> = emptyList(),
) {
    fun resolveAttributes(leader: Leader) =
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