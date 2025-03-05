package at.posselt.pfrpg2e.data.kingdom

data class RealmData(
    val size: Int,
    val worksites: WorkSites
) {
    data class WorkSites(
        val farmlands: WorkSite,
        val lumberCamps: WorkSite,
        val mines: WorkSite,
        val quarries: WorkSite,
        val luxurySources: WorkSite,
    )

    data class WorkSite(
        val quantity: Int = 0,
        val resources: Int = 0,
    ) {
        val income = quantity + resources
        operator fun plus(other: WorkSite) =
            WorkSite(
                quantity = quantity + other.quantity,
                resources = resources + other.resources,
            )
    }
}
