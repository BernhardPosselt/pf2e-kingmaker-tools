package at.posselt.pfrpg2e.kingdom.resources

import at.posselt.pfrpg2e.data.kingdom.RealmData
import at.posselt.pfrpg2e.data.kingdom.ResourceDieSize
import at.posselt.pfrpg2e.data.kingdom.findKingdomSize
import at.posselt.pfrpg2e.data.kingdom.structures.CommodityStorage

data class Income(
    val stone: Int,
    val ore: Int,
    val lumber: Int,
    val resourceDice: Int,
    val resourceDiceSize: ResourceDieSize,
    val resourcePoints: Int,
    val luxuries: Int,
) {
    val resourcePointsFormula = "$resourceDice${resourceDiceSize.value}"

    fun limitBy(storage: CommodityStorage): Income =
        copy(
            ore = ore.coerceIn(0, storage.ore),
            lumber = lumber.coerceIn(0, storage.lumber),
            stone = stone.coerceIn(0, storage.stone),
            luxuries = luxuries.coerceIn(0, storage.luxuries),
        )
}

fun calculateIncome(
    kingdomLevel: Int,
    realmData: RealmData,
    additionalResourceDice: Int,
): Income {
    val worksites = realmData.worksites
    val size = findKingdomSize(realmData.size)
    return Income(
        stone = worksites.quarries.income,
        ore = worksites.mines.income,
        lumber = worksites.lumberCamps.income,
        luxuries = worksites.luxurySources.income,
        resourceDice = 4 + kingdomLevel + additionalResourceDice,
        resourceDiceSize = size.resourceDieSize,
        resourcePoints = 0,
    )
}