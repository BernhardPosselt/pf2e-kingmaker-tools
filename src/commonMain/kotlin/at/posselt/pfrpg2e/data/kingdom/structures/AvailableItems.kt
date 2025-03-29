package at.posselt.pfrpg2e.data.kingdom.structures

data class AvailableItems(
    val other: Int = 0,
    val magical: Int = 0,
    val luxury: Int = 0,
    val divine: Int = 0,
    val primal: Int = 0,
    val arcane: Int = 0,
    val occult: Int = 0,
    val luxuryDivine: Int = 0,
    val luxuryPrimal: Int = 0,
    val luxuryArcane: Int = 0,
    val luxuryOccult: Int = 0,
    val alchemical: Int = 0,
) {
    fun toEntries() = buildMap {
        put(ExtendedItemGroup.OTHER, other)
        if (alchemical != other) {
            put(ExtendedItemGroup.ALCHEMICAL, alchemical)
        }
        if (magical != other) {
            put(ExtendedItemGroup.MAGICAL, magical)
        }
        if (luxury != other) {
            put(ExtendedItemGroup.LUXURY, luxury)
        }
        if (divine != magical) {
            put(ExtendedItemGroup.DIVINE, divine)
        }
        if (occult != magical) {
            put(ExtendedItemGroup.OCCULT, occult)
        }
        if (primal != magical) {
            put(ExtendedItemGroup.PRIMAL, primal)
        }
        if (arcane != magical) {
            put(ExtendedItemGroup.ARCANE, arcane)
        }
        if (luxuryPrimal != primal) {
            put(ExtendedItemGroup.PRIMAL_LUXURY, luxuryPrimal)
        }
        if (luxuryArcane != arcane) {
            put(ExtendedItemGroup.ARCANE_LUXURY, luxuryArcane)
        }
        if (luxuryOccult != occult) {
            put(ExtendedItemGroup.OCCULT_LUXURY, luxuryOccult)
        }
        if (luxuryDivine != divine) {
            put(ExtendedItemGroup.DIVINE_LUXURY, luxuryDivine)
        }
    }
}

fun calculateAvailableItems(
    settlementLevel: Int,
    preventItemLevelPenalty: Boolean,
    magicalItemLevelIncrease: Int,
    bonuses: AvailableItemBonuses,
): AvailableItems {
    val noShopPenalty = if (preventItemLevelPenalty) 0 else -2
    val other = settlementLevel + noShopPenalty + bonuses.other
    val magical = other + bonuses.magical + magicalItemLevelIncrease
    val divine = magical + bonuses.divine
    val arcane = magical + bonuses.arcane
    val primal = magical + bonuses.primal
    val occult = magical + bonuses.occult
    val luxury = other + bonuses.luxury
    val alchemical = other + bonuses.alchemical
    val luxuryDivine = divine + bonuses.luxury
    val luxuryArcane = arcane + bonuses.luxury
    val luxuryPrimal = primal + bonuses.luxury
    val luxuryOccult = occult + bonuses.luxury
    return AvailableItems(
        other = other,
        magical = magical,
        luxury = luxury,
        divine = divine,
        primal = primal,
        arcane = arcane,
        occult = occult,
        luxuryDivine = luxuryDivine,
        luxuryPrimal = luxuryPrimal,
        luxuryArcane = luxuryArcane,
        luxuryOccult = luxuryOccult,
        alchemical = alchemical,
    )
}