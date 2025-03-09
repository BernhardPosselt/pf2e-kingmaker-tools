package at.posselt.pfrpg2e.data.kingdom


data class RuinValues(
    val crime: RuinValue = RuinValue(),
    val corruption: RuinValue = RuinValue(),
    val strife: RuinValue = RuinValue(),
    val decay: RuinValue = RuinValue(),
) {
    fun resolve(ruin: Ruin) =
        when(ruin) {
            Ruin.CORRUPTION -> corruption
            Ruin.CRIME -> crime
            Ruin.DECAY -> decay
            Ruin.STRIFE -> strife
        }
}