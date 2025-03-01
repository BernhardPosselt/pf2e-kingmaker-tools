package at.posselt.pfrpg2e.data.kingdom.leaders

data class Vacancies(
    val ruler: Boolean = false,
    val counselor: Boolean = false,
    val emissary: Boolean = false,
    val general: Boolean = false,
    val magister: Boolean = false,
    val treasurer: Boolean = false,
    val viceroy: Boolean = false,
    val warden: Boolean = false,
) {
    fun resolveVacancy(leader: Leader) =
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