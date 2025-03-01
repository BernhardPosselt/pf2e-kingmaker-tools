package at.posselt.pfrpg2e.data.kingdom


data class Ruins(
    val decayPenalty: Int,
    val strifePenalty: Int,
    val corruptionPenalty: Int,
    val crimePenalty: Int
) {
    fun resolvePenalty(ruin: Ruin) =
        when(ruin) {
            Ruin.CORRUPTION -> corruptionPenalty
            Ruin.CRIME -> crimePenalty
            Ruin.DECAY -> decayPenalty
            Ruin.STRIFE -> strifePenalty
        }
}