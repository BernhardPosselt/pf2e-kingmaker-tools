package at.posselt.pfrpg2e.kingdom.structures

import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.AnyObject
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RawRuinAmount {
    val value: Int
    val ruin: String
    val moreThanOncePerTurn: Boolean?
}

@JsPlainObject
external interface RawReduceUnrestBy {
    val value: String
    val moreThanOncePerTurn: Boolean?
    val note: String
}

@JsPlainObject
external interface RawLeadershipActivityRule {
    val value: Int
}

@JsPlainObject
external interface RawSettlementEventsRule {
    val value: Int
}

@JsPlainObject
external interface RawActivityBonusRule {
    val value: Int
    val activity: String
}

@JsPlainObject
external interface RawAvailableItemsRule {
    val value: Int
    val group: String?
    val maximumStacks: Int?
    val alwaysStacks: Boolean
}

@JsPlainObject
external interface RawSkillBonusRule {
    val value: Int
    val skill: String
    val activity: String?
}

@JsPlainObject
external interface RawCommodityStorage {
    val ore: Int?
    val food: Int?
    val lumber: Int?
    val stone: Int?
    val luxuries: Int?
}


@JsPlainObject
external interface RawConstructionSkill {
    val skill: String
    val proficiencyRank: Int?
}

@JsPlainObject
external interface RawConstruction {
    val skills: Array<RawConstructionSkill>
    val lumber: Int?
    val luxuries: Int?
    val ore: Int?
    val stone: Int?
    val rp: Int
    val dc: Int
}

sealed external interface RawStructure

@JsPlainObject
external interface StructureRef : RawStructure {
    val ref: String
}

@JsPlainObject
external interface RawIncreaseResourceDice {
    val village: Int?
    val town: Int?
    val city: Int?
    val metropolis: Int?
}

@JsPlainObject
external interface RawStructureData : RawStructure {
    val id: String
    val name: String
    val stacksWith: String?
    val construction: RawConstruction?
    val notes: String?
    val preventItemLevelPenalty: Boolean?
    val enableCapitalInvestment: Boolean?
    val skillBonusRules: Array<RawSkillBonusRule>?
    val activityBonusRules: Array<RawActivityBonusRule>?
    val availableItemsRules: Array<RawAvailableItemsRule>?
    val settlementEventRules: Array<RawSettlementEventsRule>?
    val leadershipActivityRules: Array<RawLeadershipActivityRule>?
    val storage: RawCommodityStorage?
    val increaseLeadershipActivities: Boolean?
    val isBridge: Boolean?
    val consumptionReduction: Int?
    val unlockActivities: Array<String>?
    val traits: Array<String>?
    val lots: Int
    val affectsEvents: Boolean?
    val affectsDowntime: Boolean?
    val reducesUnrest: Boolean?
    val reducesRuin: Boolean?
    val level: Int
    val upgradeFrom: Array<String>?
    val reduceUnrestBy: RawReduceUnrestBy?
    val reduceRuinBy: RawRuinAmount?
    val gainRuin: RawRuinAmount?
    val increaseResourceDice: RawIncreaseResourceDice?
    val consumptionReductionStacks: Boolean?
    val ignoreConsumptionReductionOf: Array<String>?
    val maximumCivicRdLimit: Int?
    val increaseMinimumSettlementActions: Int?
}

@JsModule("./structures.json")
external val structures: Array<RawStructureData>

@JsModule("./schemas/structure.json")
external val structureSchema: AnyObject

@JsModule("./schemas/structure-ref.json")
external val structureRefSchema: AnyObject

private fun RawStructureData.translate() =
    RawStructureData.copy(
        this,
        name = t(name),
        notes = notes?.let { t(it) },
    )

val vkStructureIds = setOf(
    "bank-vk",
    "castle-vk",
    "construction-yard-vk",
    "festival-hall-vk",
    "fishing-fleets-vk",
    "garrison-vk",
    "granary-vk",
    "inn-vk",
    "library-vk",
    "magic-shop-vk",
    "monument-vk",
    "occult-shop-vk",
    "palace-vk",
    "pier-vk",
    "smithy-vk",
    "tavern-dive-vk",
    "tavern-luxury-vk",
    "tavern-popular-vk",
    "tavern-world-class-vk",
    "town-hall-vk",
    "waterfront-vk",
)

val vkToBaseStructureIds = mapOf(
    "bank-vk" to "bank",
    "castle-vk" to "castle",
    "construction-yard-vk" to "construction-yard",
    "festival-hall-vk" to "festival-hall",
    "fishing-fleets-vk" to "fishing-fleets",
    "garrison-vk" to "garrison",
    "granary-vk" to "granary",
    "inn-vk" to "inn",
    "library-vk" to "library",
    "magic-shop-vk" to "magic-shop",
    "monument-vk" to "monument",
    "occult-shop-vk" to "occult-shop",
    "palace-vk" to "palace",
    "pier-vk" to "pier",
    "smithy-vk" to "smithy",
    "tavern-dive-vk" to "tavern-dive",
    "tavern-luxury-vk" to "tavern-luxury",
    "tavern-popular-vk" to "tavern-popular",
    "tavern-world-class-vk" to "tavern-world-class",
    "town-hall-vk" to "town-hall",
    "waterfront-vk" to "waterfront",
)

var translatedStructures = emptyArray<RawStructureData>()

fun translateStructureData() {
    translatedStructures = structures
        .map { it.translate() }
        .toTypedArray()
}