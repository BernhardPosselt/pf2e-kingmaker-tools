package at.posselt.pfrpg2e.data.kingdom.structures

import kotlin.math.max
import kotlin.math.min

data class Structure(
    val id: String,
    val uuid: String,
    val name: String,
    val img: String? = null,
    val stacksWith: String? = null,
    val construction: Construction = Construction(),
    val notes: String? = null,
    val preventItemLevelPenalty: Boolean = false,
    val enableCapitalInvestment: Boolean = false,
    val bonuses: Set<StructureBonus> = emptySet(),
    val availableItemsRules: Set<AvailableItemsRule> = emptySet(),
    val settlementEventBonus: Int = 0,
    val leadershipActivityBonus: Int = 0,
    val storage: CommodityStorage = CommodityStorage(),
    val increaseLeadershipActivities: Boolean = false,
    val isBridge: Boolean = false,
    val consumptionReduction: Int = 0,
    val unlockActivities: Set<String> = emptySet(),
    val traits: Set<StructureTrait> = emptySet(),
    val lots: Int = 0,
    val affectsEvents: Boolean = false,
    val affectsDowntime: Boolean = false,
    val reducesUnrest: Boolean = false,
    val reducesRuin: Boolean = false,
    val level: Int = 0,
    val currentRp: Int = 0,
    val constructedRp: Int = 0,
    val upgradeFrom: Set<String> = emptySet(),
    val reduceUnrestBy: ReduceUnrestBy? = null,
    val reduceRuinBy: RuinAmount? = null,
    val gainRuin: RuinAmount? = null,
    val increaseResourceDice: IncreaseResourceDice = IncreaseResourceDice(),
    val consumptionReductionStacks: Boolean = false,
    val inConstruction: Boolean = false,
    val ignoreConsumptionReductionOf: Set<String> = emptySet(),
    val maximumCivicRdLimit: Int = 0,
    val increaseMinimumSettlementActions: Int = 0,
) {
    val isResidential = traits.contains(StructureTrait.RESIDENTIAL)
    val rpPaid = currentRp >= constructedRp
    val remainingRp = max(0, constructedRp - currentRp)

    fun calculateInitialRpCost(maxRpPerTurn: Int) =
        min(constructedRp, maxRpPerTurn)

    fun calculateTurnRpCost(maxRpPerTurn: Int) =
        min(remainingRp, maxRpPerTurn)
}



