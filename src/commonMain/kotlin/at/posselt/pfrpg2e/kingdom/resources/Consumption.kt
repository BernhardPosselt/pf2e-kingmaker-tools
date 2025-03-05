package at.posselt.pfrpg2e.kingdom.resources

import at.posselt.pfrpg2e.data.kingdom.RealmData
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import kotlin.math.max

data class Consumption(
    val total: Int,
    val surplus: Int,
    val farmlands: Int,
    val food: Int,
    val armies: Int,
)

fun calculateConsumption(
    settlements: List<Settlement>,
    realmData: RealmData,
    armyConsumption: Int,
): Consumption {
    val farmlands = realmData.worksites.farmlands.quantity
    val food = realmData.worksites.farmlands.resources
    val settlementConsumption = settlements.sumOf { it.consumption }
    val surplus = settlements.sumOf { it.consumptionSurplus }
    val total = settlementConsumption + armyConsumption
    return Consumption(
        total = max(0, total),
        surplus = surplus,
        farmlands = farmlands,
        food = food,
        armies = armyConsumption,
    )
}