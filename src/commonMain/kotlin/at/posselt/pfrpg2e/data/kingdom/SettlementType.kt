package at.posselt.pfrpg2e.data.kingdom

data class SettlementType(
	val name: String,
	val kingdomLevel: Int,
	val sizeBlocks: Int,
	val populationMin: Int,
	val populationMax: Int?,
)

val settlementTypeData = listOf(
	SettlementType(name = "Village", kingdomLevel = 1, sizeBlocks = 1, populationMin = 0, populationMax = 400),
	SettlementType(name = "Town", kingdomLevel = 3, sizeBlocks = 2, populationMin = 401, populationMax = 2000),
	SettlementType(name = "City", kingdomLevel = 9, sizeBlocks = 5, populationMin = 2001, populationMax = 25000),
	SettlementType(name = "Metropolis", kingdomLevel = 15, sizeBlocks = 10, populationMin = 25001, populationMax = null),
)

fun findSettlementType(name: String): SettlementType? =
	settlementTypeData.find { it.name.equals(name, ignoreCase = true) }
