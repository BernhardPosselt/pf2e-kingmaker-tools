package at.posselt.pfrpg2e.data.kingdom.settlements

data class Settlement(
    val name: String,
    val waterBorders: Int,
    val hasBridge: Boolean,
    val isSecondaryTerritory: Boolean,
)
