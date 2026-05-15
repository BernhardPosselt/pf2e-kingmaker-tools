package at.posselt.pfrpg2e.data.kingdom.settlements

import at.posselt.pfrpg2e.data.kingdom.structures.AvailableItemBonuses
import at.posselt.pfrpg2e.data.kingdom.structures.CommodityStorage
import at.posselt.pfrpg2e.data.kingdom.structures.GroupedStructureBonus
import at.posselt.pfrpg2e.data.kingdom.structures.Structure
import kotlin.math.abs
import kotlin.math.max

data class Block(
    val delayedStructures: List<Structure>,
    val constructedStructures: List<Structure>,
    val structuresUnderConstruction: List<Structure>,
) {
    val occupiedLots = constructedStructures.sumOf { it.lots }
    val isOccupied = occupiedLots > 0
}

data class Settlement(
    val id: String,
    val name: String,
    val type: SettlementType,
    val waterBorders: Int,
    val isSecondaryTerritory: Boolean,
    val settlementEventBonus: Int, // added by watchtowers
    val leaderLeadershipActivityBonus: Int, // added by Palace & co
    val bonuses: Set<GroupedStructureBonus>,
    val allowCapitalInvestment: Boolean,
    val notes: Set<String>,
    val storage: CommodityStorage,
    val increaseLeadershipActivities: Boolean,
    val consumptionReduction: Int,
    val availableItems: AvailableItemBonuses,
    val size: SettlementSize,
    val unlockActivities: Set<String>,
    val residentialLots: Int,
    val hasBridge: Boolean,
    val occupiedBlocks: Int,
    val preventItemLevelPenalty: Boolean,
    val delayedStructures: List<Structure>,
    val constructedStructures: List<Structure>,
    val structuresUnderConstruction: List<Structure>,
    val maximumCivicRdLimit: Int,
    val settlementActions: Int,
    val blocks: List<Block>,
) {
    private val totalConsumption = size.consumption - consumptionReduction
    val isOvercrowded = occupiedBlocks > residentialLots
    val lacksBridge = waterBorders >= 4 && !hasBridge
    val consumption = max(0, totalConsumption)
    val consumptionSurplus = totalConsumption
        .takeIf { it < 0 }
        ?.let { abs(it) }
        ?: 0
    val highestUniqueBonuses: Set<GroupedStructureBonus>
        get() {
            val bonusesByType = bonuses.groupBy { it.skill to it.activity }
            return bonusesByType
                .values.map { structures ->
                    structures.maxBy { it.value }
                }
                .toSet()
        }
    val level = max(1, occupiedBlocks)

    fun canLevelUp(kingdomLevel: Int, capitalCanGrowOneSizeLarger: Boolean): SettlementLevelUpType? {
        // you can never level up if the settlement is overcrowded
        if (isOvercrowded) return null
        val isCapitalAndLarger = capitalCanGrowOneSizeLarger && type == SettlementType.CAPITAL
        val satisfiesTown = kingdomLevel >= 3 || isCapitalAndLarger
        val satisfiesCity = kingdomLevel >= 9 || (kingdomLevel >= 3 && isCapitalAndLarger)
        val satisfiesMetropolis = kingdomLevel >= 15 || (kingdomLevel >= 9 && isCapitalAndLarger)
        val blocksTotal = blocks.size
        return if (blocksTotal == 1 && blocks.sumOf { it.occupiedLots } == 4 && satisfiesTown) {
            SettlementLevelUpType.TOWN
        } else {
            val blocksWithAtLeast2OccupiedLots = blocks.filter { it.occupiedLots >= 2 }.size
            if (blocksTotal == 4 && blocksWithAtLeast2OccupiedLots == 4 && satisfiesCity) {
                SettlementLevelUpType.CITY
            } else if (blocksTotal == 9 && blocksWithAtLeast2OccupiedLots == 9 && satisfiesMetropolis) {
                SettlementLevelUpType.METROPOLIS
            } else if (blocksTotal == 18 && blocksWithAtLeast2OccupiedLots == 18 && satisfiesMetropolis) {
                SettlementLevelUpType.METROPOLIS_THIRD_GRID
            } else if (blocksTotal == 27 && blocksWithAtLeast2OccupiedLots == 27 && satisfiesMetropolis) {
                SettlementLevelUpType.METROPOLIS_FOURTH_GRID
            } else {
                null
            }
        }
    }

    fun nextLevelUp(): SettlementLevelUpType? = when(blocks.size) {
        1 -> SettlementLevelUpType.TOWN
        4 -> SettlementLevelUpType.CITY
        9 -> SettlementLevelUpType.METROPOLIS
        18 -> SettlementLevelUpType.METROPOLIS_THIRD_GRID
        27 -> SettlementLevelUpType.METROPOLIS_FOURTH_GRID
        else -> null
    }
}
