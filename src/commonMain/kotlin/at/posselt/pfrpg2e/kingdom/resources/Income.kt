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
            ore = storage.limitOre(ore),
            lumber = storage.limitLumber(lumber),
            stone = storage.limitStone(stone),
            luxuries = storage.limitLuxuries(luxuries),
        )
}

fun calculateIncome(
    realmData: RealmData,
    resourceDice: Int,
    increaseGainedLuxuries: Int,
): Income {
    val worksites = realmData.worksites
    val size = findKingdomSize(realmData.size)
    val luxuries = worksites.luxurySources.income
    return Income(
        stone = worksites.quarries.income,
        ore = worksites.mines.income,
        lumber = worksites.lumberCamps.income,
        luxuries = if(luxuries > 0) luxuries + increaseGainedLuxuries else luxuries,
        resourceDice = resourceDice,
        resourceDiceSize = size.resourceDieSize,
        resourcePoints = 0,
    )
}