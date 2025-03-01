package at.posselt.pfrpg2e.data.kingdom

private fun calculateAbilityModifier(score: Int) = (score - 10) / 2

class KingdomAbilityScores(
    val economy: Int,
    val stability: Int,
    val loyalty: Int,
    val culture: Int,
) {
    val cultureModifier = calculateAbilityModifier(culture)
    val loyaltyModifier = calculateAbilityModifier(loyalty)
    val economyModifier = calculateAbilityModifier(economy)
    val stabilityModifier = calculateAbilityModifier(stability)

    fun resolve(ability: KingdomAbility) =
        when (ability) {
            KingdomAbility.CULTURE -> culture
            KingdomAbility.ECONOMY -> economy
            KingdomAbility.LOYALTY -> loyalty
            KingdomAbility.STABILITY -> stability
        }

    fun resolveModifier(ability: KingdomAbility) =
        when (ability) {
            KingdomAbility.CULTURE -> cultureModifier
            KingdomAbility.ECONOMY -> economyModifier
            KingdomAbility.LOYALTY -> loyaltyModifier
            KingdomAbility.STABILITY -> stabilityModifier
        }
}