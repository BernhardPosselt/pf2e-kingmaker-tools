package at.posselt.pfrpg2e.data.kingdom.structures

data class ReduceUnrestBy(
    val value: String,
    val moreThanOncePerTurn: Boolean,
    val note: String?,
)