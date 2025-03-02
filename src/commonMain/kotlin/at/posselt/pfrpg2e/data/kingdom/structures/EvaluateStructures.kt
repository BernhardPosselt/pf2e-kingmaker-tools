package at.posselt.pfrpg2e.data.kingdom.structures

import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.data.kingdom.settlements.findSettlementSize
import kotlin.math.abs
import kotlin.math.max

private data class CombinedBonuses(
    val bonuses: List<GroupedStructureBonus>,
    val eventBonus: Int,
    val leaderBonus: Int,
)

private fun calculateConsumptionReduction(structures: List<Structure>): Int {
    val allowedNames = structures.map { it.name }.toSet() -
            structures.flatMap { it.ignoreConsumptionReductionOf }.toSet()
    val structuresToSum = structures.filter { allowedNames.contains(it.name) }
    val nonStacking = structuresToSum
        .filterNot { it.consumptionReductionStacks }
        .distinctBy { it.name }
    val stacking = structuresToSum
        .filter { it.consumptionReductionStacks }
    return (nonStacking + stacking)
        .sumOf { it.consumptionReduction }
}

private fun combineBonuses(
    structures: List<Structure>,
    allStructuresStack: Boolean,
    maxItemBonus: Int,
): CombinedBonuses {
    val leaderBonus = structures.maxOfOrNull { it.leadershipActivityBonus } ?: 0
    val eventBonus = structures.sumOf { it.settlementEventBonus }.coerceIn(0, maxItemBonus)
    val grouped = structures.groupBy { if (allStructuresStack) "" else (it.stacksWith ?: it.name) }
    val bonuses = grouped.flatMap { groupedStructures ->
        val names = groupedStructures.value.map { it.name }
        groupedStructures.value.flatMap { bonus ->
            val groupedBonuses = bonus.bonuses.groupBy { it }
            groupedBonuses.values.map {
                val bonus = it.first().copy(value = it.size.coerceIn(0, maxItemBonus))
                GroupedStructureBonus(
                    structureNames = names,
                    skill = bonus.skill,
                    activity = bonus.activity,
                    value = bonus.value
                )
            }
        }
    }
    return CombinedBonuses(
        eventBonus = eventBonus,
        leaderBonus = leaderBonus,
        bonuses = bonuses,
    )
}


fun evaluateStructures(
    settlementName: String,
    settlementLevel: Int,
    waterBorders: Int,
    occupiedBlocks: Int,
    isSecondaryTerritory: Boolean,
    structures: List<Structure>,
    allStructuresStack: Boolean,
): Settlement {
    val settlementSize = findSettlementSize(settlementLevel)
    val consumptionReduction = calculateConsumptionReduction(structures)
    val settlementConsumption = settlementSize.consumption - consumptionReduction
    val maxItemBonus = settlementSize.maxItemBonus
    val (bonuses, eventBonus, leaderBonus) = combineBonuses(structures, allStructuresStack, maxItemBonus)
    return Settlement(
        name = settlementName,
        waterBorders = waterBorders,
        isSecondaryTerritory = isSecondaryTerritory,
        settlementEventBonus = eventBonus,
        leaderLeadershipActivityBonus = leaderBonus,
        bonuses = bonuses,
        allowCapitalInvestment = structures.any { it.enableCapitalInvestment },
        notes = structures
            .mapNotNull { it.notes }
            .toSet(),
        storage = structures
            .map { it.storage }
            .fold(CommodityStorage()) { acc, el -> acc + el },
        increaseLeadershipActivities = structures.any { it.increaseLeadershipActivities },
        settlementConsumption = max(0, settlementConsumption),
        consumptionReduction = consumptionReduction,
        consumptionSurplus = settlementConsumption
            .takeIf { it < 0 }
            ?.let { abs(it) }
            ?: 0,
        settlementSize = settlementSize,
        unlockActivities = structures
            .flatMap { it.unlockActivities }
            .toSet(),
        residentialLots = structures
            .filter { it.traits.contains(StructureTrait.RESIDENTIAL) }
            .sumOf { it.lots },
        hasBridge = structures.any { it.isBridge },
        occupiedBlocks = occupiedBlocks,
    )
}
