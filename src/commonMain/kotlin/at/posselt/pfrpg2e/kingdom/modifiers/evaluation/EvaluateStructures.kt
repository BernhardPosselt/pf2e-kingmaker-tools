package at.posselt.pfrpg2e.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.data.kingdom.settlements.SettlementType
import at.posselt.pfrpg2e.data.kingdom.settlements.findSettlementSize
import at.posselt.pfrpg2e.data.kingdom.structures.AvailableItemBonuses
import at.posselt.pfrpg2e.data.kingdom.structures.CommodityStorage
import at.posselt.pfrpg2e.data.kingdom.structures.GroupedStructureBonus
import at.posselt.pfrpg2e.data.kingdom.structures.ItemGroup
import at.posselt.pfrpg2e.data.kingdom.structures.Structure

private data class CombinedBonuses(
    val bonuses: Set<GroupedStructureBonus>,
    val eventBonus: Int,
    val leaderBonus: Int,
)

private fun calculateConsumptionReduction(structures: List<Structure>): Int {
    val allowedIds = structures.map { it.id }.toSet() -
            structures.flatMap { it.ignoreConsumptionReductionOf }.toSet()
    val structuresToSum = structures.filter { allowedIds.contains(it.id) }
    val nonStacking = structuresToSum
        .filterNot { it.consumptionReductionStacks }
        .distinctBy { it.stacksWith ?: it.id }
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
    val grouped = structures.groupBy { if (allStructuresStack) "" else (it.stacksWith ?: it.id) }
    val structuresById = structures.associateBy { it.id }
    val bonuses = grouped.flatMap { groupedStructures ->
        val structures = groupedStructures.value
        val ids = structures.map { it.stacksWith ?: it.id }
        val structuresByBonus = structures
            .flatMap { structure -> structure.bonuses.map { it to structure } }
            .groupBy { it.first.skill to it.first.activity }
            .mapValues { (_, structures) -> structures.map { it.second.stacksWith ?: it.second.id } }
        val groupedBonuses = structures.flatMap { it.bonuses }.groupBy { it }
        groupedBonuses.values.map { bonuses ->
            val value = bonuses.sumOf { it.value }.coerceIn(0, maxItemBonus)
            val bonus = bonuses.first()
            val namesIndex = bonus.skill to bonus.activity
            GroupedStructureBonus(
                structureNames = ids
                    .mapNotNull { structuresByBonus[namesIndex] }
                    .flatMap { it }
                    .mapNotNull { structuresById[it]?.name }
                    .toSet(),
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

fun parseAvailableItems(structures: List<Structure>): AvailableItemBonuses {
    val structuresById = structures
        .groupBy { it.stacksWith ?: it.id }
    val bonusesPerGroup = structuresById
        .map { (_, structure) -> calculateItemsFromSameStructures(structure, alwaysStacks = false) }
        // for each structure, we only want the maximum bonus per category
        .fold(mapOf<ItemGroup, Int>()) { prev, curr ->
            ItemGroup.entries.associateWith {
                val previousBonus = prev[it] ?: 0
                val currentBonus = curr[it] ?: 0
                if (currentBonus > previousBonus) currentBonus else previousBonus
            }
        }
    val bonusesPerGroupThatAlwaysStack = structuresById
        .map { (_, structure) -> calculateItemsFromSameStructures(structure, alwaysStacks = true) }
        // for each structure, we only want the maximum bonus per category
        .fold(mapOf<ItemGroup, Int>()) { prev, curr ->
            ItemGroup.entries.associateWith {
                val previousBonus = prev[it] ?: 0
                val currentBonus = curr[it] ?: 0
                previousBonus + currentBonus
            }
        }
    return AvailableItemBonuses(
        other = (bonusesPerGroup[ItemGroup.OTHER] ?: 0) +
                (bonusesPerGroupThatAlwaysStack[ItemGroup.OTHER] ?: 0),
        magical = (bonusesPerGroup[ItemGroup.MAGICAL] ?: 0) +
                (bonusesPerGroupThatAlwaysStack[ItemGroup.MAGICAL] ?: 0),
        luxury = (bonusesPerGroup[ItemGroup.LUXURY] ?: 0) +
                (bonusesPerGroupThatAlwaysStack[ItemGroup.LUXURY] ?: 0),
        divine = (bonusesPerGroup[ItemGroup.DIVINE] ?: 0) +
                (bonusesPerGroupThatAlwaysStack[ItemGroup.DIVINE] ?: 0),
        primal = (bonusesPerGroup[ItemGroup.PRIMAL] ?: 0) +
                (bonusesPerGroupThatAlwaysStack[ItemGroup.PRIMAL] ?: 0),
        arcane = (bonusesPerGroup[ItemGroup.ARCANE] ?: 0) +
                (bonusesPerGroupThatAlwaysStack[ItemGroup.ARCANE] ?: 0),
        occult = (bonusesPerGroup[ItemGroup.OCCULT] ?: 0) +
                (bonusesPerGroupThatAlwaysStack[ItemGroup.OCCULT] ?: 0),
        alchemical = (bonusesPerGroup[ItemGroup.ALCHEMICAL] ?: 0) +
                (bonusesPerGroupThatAlwaysStack[ItemGroup.ALCHEMICAL] ?: 0),
    )
}

/**
 * Item Group increases stack, but only if they are from the same structure up to a maximum Stacks limit
 */
private fun calculateItemsFromSameStructures(
    sameStructureInstances: List<Structure>,
    alwaysStacks: Boolean,
): Map<ItemGroup, Int> {
    val rulesByType = sameStructureInstances
        .flatMap { it.availableItemsRules }
        .groupBy { it.group ?: ItemGroup.OTHER }
    val result = rulesByType
        .mapValues { (_, rules) ->
            rules.filter { it.alwaysStacks == alwaysStacks }
                .groupBy { it.maximumStacks }
                .mapValues { (maximum, rules) ->
                    rules.sortedByDescending { it.value }.take(maximum)
                }
                .values
                .flatMap { it }
                .sumOf { it.value }
        }
    return result
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
    val type: SettlementType,
    val isSecondaryTerritory: Boolean,
    val waterBorders: Int,
    val id: String,
)

fun evaluateSettlement(
    data: SettlementData,
    structures: List<Structure>,
    allStructuresStack: Boolean,
    allowCapitalInvestmentInCapitalWithoutBank: Boolean,
): Settlement {
    val settlementSize = findSettlementSize(data.occupiedBlocks)
    val maxItemBonus = settlementSize.maxItemBonus
    val constructedStructures = structures.filter { !it.slowed && it.rpPaid }
    val slowedStructures = structures.filter { it.slowed }
    val underConstructionStructures = structures.filter { !it.slowed && !it.rpPaid }
    val consumptionReduction = calculateConsumptionReduction(constructedStructures)
    val (bonuses, eventBonus, leaderBonus) = combineBonuses(
        constructedStructures,
        allStructuresStack,
        maxItemBonus,
        data.name,
    )
    val storage = constructedStructures
        .map { it.storage }
        .fold(CommodityStorage()) { acc, el -> acc + el }
    val allowCapitalInvestment = constructedStructures.any { it.enableCapitalInvestment } ||
            (data.type == SettlementType.CAPITAL && allowCapitalInvestmentInCapitalWithoutBank)
    val notes = constructedStructures
        .mapNotNull { it.notes }
        .toSet()
    val increaseLeadershipActivities = (data.type == SettlementType.CAPITAL
            && constructedStructures.any { it.increaseLeadershipActivities })
    val availableItems = parseAvailableItems(constructedStructures)
    val residentialLots = constructedStructures
        .filter { it.isResidential }
        .sumOf { it.lots }
    val unlockActivities = constructedStructures
        .flatMap { it.unlockActivities }
        .toSet()
    val hasBridge = constructedStructures.any { it.isBridge }
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
        delayedStructures = slowedStructures,
        constructedStructures = constructedStructures,
        structuresUnderConstruction = underConstructionStructures,
        availableItems = availableItems,
        preventItemLevelPenalty = constructedStructures.any { it.preventItemLevelPenalty },
        maximumCivicRdLimit = structures.maxOfOrNull { it.maximumCivicRdLimit } ?: 0,
        settlementActions = structures.maxOfOrNull { it.increaseMinimumSettlementActions } ?: 0,
    )
}
