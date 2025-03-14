package at.posselt.pfrpg2e.data.kingdom.structures

import at.posselt.pfrpg2e.data.kingdom.Ruin

data class RuinAmount(
    val value: Int,
    val ruin: Ruin?,
    val moreThanOncePerTurn: Boolean,
)