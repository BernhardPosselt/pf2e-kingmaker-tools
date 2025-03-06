package at.posselt.pfrpg2e.kingdom.unrest

data class GainedUnrest(
    val war: Int,
    val overcrowded: Int,
    val secondaryTerritory: Int,
    val rulerVacant: Boolean,
)