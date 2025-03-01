package at.posselt.pfrpg2e.data.kingdom.leaders

data class LeaderLevels (
    val ruler: Int = 0,
    val counselor: Int = 0,
    val emissary: Int = 0,
    val general: Int = 0,
    val magister: Int = 0,
    val treasurer: Int = 0,
    val viceroy: Int = 0,
    val warden: Int = 0,
) {
    fun resolveLevel(leader: Leader) =
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