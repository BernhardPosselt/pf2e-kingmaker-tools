package at.posselt.pfrpg2e.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.data.kingdom.settlements.SettlementType
import at.posselt.pfrpg2e.data.kingdom.settlements.findSettlementSize
import at.posselt.pfrpg2e.data.kingdom.structures.CommodityStorage
import at.posselt.pfrpg2e.data.kingdom.structures.GroupedStructureBonus
import at.posselt.pfrpg2e.data.kingdom.structures.Structure

private data class CombinedBonuses(
    val bonuses: Set<GroupedStructureBonus>,
    val eventBonus: Int,
    val leaderBonus: Int,
)

private fun calculateConsumptionReduction(structures: List<Structure>): Int {
    val allowedNames = structures.map { it.name }.toSet() -
            structures.flatMap { it.ignoreConsumptionReductionOf }.toSet()
    val structuresToSum = structures.filter { allowedNames.contains(it.name) }
    val nonStacking = structuresToSum
        .filterNot { it.consumptionReductionStacks }
        .distinctBy { it.stacksWith ?: it.name }
    val stacking = structuresToSum
        .filter { it.consumptionReductionStacks }
    return (nonStacking + stacking)
        .sumOf { it.consumptionReduction }
}

private fun combineBonuses(
    structures: List<Structure>,
    allStructuresStack: Boolean,
    maxItemBonus: Int,
    settlementName: String,
): CombinedBonuses {
    val leaderBonus = structures.maxOfOrNull { it.leadershipActivityBonus } ?: 0
    val eventBonus = structures.sumOf { it.settlementEventBonus }.coerceIn(0, maxItemBonus)
    val grouped = structures.groupBy { if (allStructuresStack) "" else (it.stacksWith ?: it.name) }
    val bonuses = grouped.flatMap { groupedStructures ->
        val structures = groupedStructures.value
        val groupedBonuses = structures.flatMap { it.bonuses }.groupBy { it }
        val names = structures.map { it.stacksWith ?: it.name }.toSet()
        groupedBonuses.values.map { bonuses ->
            val value = bonuses.sumOf { it.value }.coerceIn(0, maxItemBonus)
            val bonus = bonuses.first()
            GroupedStructureBonus(
                structureNames = names,
                skill = bonus.skill,
                activity = bonus.activity,
                value = value,
                locatedIn = settlementName,
            )
        }
    }.toSet()
    return CombinedBonuses(
        eventBonus = eventBonus,
        leaderBonus = leaderBonus,
        bonuses = bonuses,
    )
}

value class MergedSettlement(val settlement: Settlement)

fun includeCapital(
    settlement: Settlement,
    capital: Settlement?,
    capitalModifierFallbackEnabled: Boolean,
): MergedSettlement =
    MergedSettlement(
        settlement.copy(
            bonuses = if (capitalModifierFallbackEnabled && capital != null) {
                (settlement.bonuses + capital.bonuses)
            } else {
                settlement.bonuses
            },
        )
    )

data class GlobalStructureBonuses(
    val unlockedActivities: Set<String>,
    val leaderLeadershipActivityBonus: Int,
    val increaseLeadershipActivities: Boolean,
)

fun evaluateGlobalBonuses(settlements: List<Settlement>) =
    GlobalStructureBonuses(
        unlockedActivities = settlements.flatMap { it.unlockActivities }.toSet(),
        leaderLeadershipActivityBonus = settlements
            .maxOfOrNull { it.leaderLeadershipActivityBonus } ?: 0,
        increaseLeadershipActivities = settlements.any { it.increaseLeadershipActivities },
    )

data class SettlementData(
    val name: String,
    val occupiedBlocks: Int,
    val level: Int,
    val type: SettlementType,
    val isSecondaryTerritory: Boolean,
    val waterBorders: Int,
    val id: String,
)

fun evaluateSettlement(
    data: SettlementData,
    structures: List<Structure>,
    allStructuresStack: Boolean,
): Settlement {
    val settlementSize = findSettlementSize(data.level)
    val consumptionReduction = calculateConsumptionReduction(structures)
    val maxItemBonus = settlementSize.maxItemBonus
    val (bonuses, eventBonus, leaderBonus) = combineBonuses(
        structures,
        allStructuresStack,
        maxItemBonus,
        data.name,
    )
    val storage = structures
        .map { it.storage }
        .fold(CommodityStorage()) { acc, el -> acc + el }
    val allowCapitalInvestment = structures.any { it.enableCapitalInvestment }
    val notes = structures
        .mapNotNull { it.notes }
        .toSet()
    val increaseLeadershipActivities = (data.type == SettlementType.CAPITAL
            && structures.any { it.increaseLeadershipActivities })

    val residentialLots = structures
        .filter { it.isResidential }
        .sumOf { it.lots }
    val unlockActivities = structures
        .flatMap { it.unlockActivities }
        .toSet()
    val hasBridge = structures.any { it.isBridge }
    return Settlement(
        id = data.id,
        name = data.name,
        waterBorders = data.waterBorders,
        isSecondaryTerritory = data.isSecondaryTerritory,
        settlementEventBonus = eventBonus,
        leaderLeadershipActivityBonus = leaderBonus,
        bonuses = bonuses,
        allowCapitalInvestment = allowCapitalInvestment,
        notes = notes,
        storage = storage,
        increaseLeadershipActivities = increaseLeadershipActivities,
        consumptionReduction = consumptionReduction,
        size = settlementSize,
        unlockActivities = unlockActivities,
        residentialLots = residentialLots,
        hasBridge = hasBridge,
        occupiedBlocks = data.occupiedBlocks,
        type = data.type,
    )
}
