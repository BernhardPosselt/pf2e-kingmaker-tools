package at.posselt.pfrpg2e.data.kingdom.structures

data class AvailableItemBonuses(
    val other: Int = 0,
    val magical: Int = 0,
    val luxury: Int = 0,
    val divine: Int = 0,
    val primal: Int = 0,
    val arcane: Int = 0,
    val occult: Int = 0,
    val alchemical: Int = 0,
)