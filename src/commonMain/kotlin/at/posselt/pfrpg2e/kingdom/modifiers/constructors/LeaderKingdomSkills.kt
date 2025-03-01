package at.posselt.pfrpg2e.kingdom.modifiers.constructors

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.Leader

data class LeaderKingdomSkills(
    val ruler: List<KingdomSkill> = emptyList(),
    val counselor: List<KingdomSkill> = emptyList(),
    val emissary: List<KingdomSkill> = emptyList(),
    val general: List<KingdomSkill> = emptyList(),
    val magister: List<KingdomSkill> = emptyList(),
    val treasurer: List<KingdomSkill> = emptyList(),
    val viceroy: List<KingdomSkill> = emptyList(),
    val warden: List<KingdomSkill> = emptyList(),
) {
    fun resolveAttributes(leader: Leader) =
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