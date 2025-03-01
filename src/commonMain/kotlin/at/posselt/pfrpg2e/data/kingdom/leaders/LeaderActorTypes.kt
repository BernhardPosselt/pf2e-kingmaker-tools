package at.posselt.pfrpg2e.data.kingdom.leaders

data class LeaderActorTypes (
    val ruler: LeaderType = LeaderType.PC,
    val counselor: LeaderType = LeaderType.PC,
    val emissary: LeaderType = LeaderType.PC,
    val general: LeaderType = LeaderType.PC,
    val magister: LeaderType = LeaderType.PC,
    val treasurer: LeaderType = LeaderType.PC,
    val viceroy: LeaderType = LeaderType.PC,
    val warden: LeaderType = LeaderType.PC,
) {
    fun resolveType(leader: Leader) =
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