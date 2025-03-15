package at.posselt.pfrpg2e.kingdom.resources

import at.posselt.pfrpg2e.data.kingdom.RealmData
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement

data class Consumption(
    val now: Int,
    val total: Int,
    val settlementSurplus: Int,
    val farmlands: Int,
    val food: Int,
    val armies: Int,
    val settlements: Int,
    val resourcesSurplus: Int,
)

fun calculateConsumption(
    settlements: List<Settlement>,
    realmData: RealmData,
    armyConsumption: Int,
    now: Int,
): Consumption {
    val farmlands = realmData.worksites.farmlands.quantity
    val food = realmData.worksites.farmlands.resources
    val settlementConsumption = settlements.sumOf { it.consumption }
    val settlementSurplus = settlements.sumOf { it.consumptionSurplus }
    val consumers = now + settlementConsumption + armyConsumption
    val total = (consumers - food - farmlands).coerceIn(0, Int.MAX_VALUE)
    val resourcesSurplus = (farmlands + food - consumers).coerceIn(0, Int.MAX_VALUE)
    return Consumption(
        total = total,
        resourcesSurplus = resourcesSurplus,
        settlementSurplus = settlementSurplus,
        farmlands = farmlands,
        food = food,
        armies = armyConsumption,
        settlements = settlementConsumption,
        now = now,
    )
}