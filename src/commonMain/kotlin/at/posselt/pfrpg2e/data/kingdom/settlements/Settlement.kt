package at.posselt.pfrpg2e.data.kingdom.settlements

import at.posselt.pfrpg2e.data.kingdom.structures.CommodityStorage
import at.posselt.pfrpg2e.data.kingdom.structures.GroupedStructureBonus
import at.posselt.pfrpg2e.data.kingdom.structures.Structure
import kotlin.math.abs
import kotlin.math.max

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
    val size: SettlementSize,
    val unlockActivities: Set<String>,
    val residentialLots: Int,
    val hasBridge: Boolean,
    val occupiedBlocks: Int,
    val structuresInConstruction: List<Structure>,
    val constructedStructures: List<Structure>,
) {
    private val totalConsumption = size.consumption - consumptionReduction
    val isOvercrowded = occupiedBlocks > residentialLots
    val lacksBridge = waterBorders >= 4 && !hasBridge
    val consumption = max(0, totalConsumption)
    val consumptionSurplus = totalConsumption
        .takeIf { it < 0 }
        ?.let { abs(it) }
        ?: 0
}
