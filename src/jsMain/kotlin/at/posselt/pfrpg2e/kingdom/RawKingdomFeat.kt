package at.posselt.pfrpg2e.kingdom

import js.objects.JsPlainObject
import js.objects.Record
import kotlinx.serialization.json.JsonElement


@JsPlainObject
external interface UpgradeResult {
    val upgrade: String
    val predicate: Array<RawPredicate>?
}

@JsPlainObject
external interface RawKingdomFeat {
    val name: String
    val level: Int
    val text: String
    val prerequisites: String?
    val automationNotes: String?
    val modifiers: Array<RawModifier>?
    val resourceDice: Int?
    val settlementItemLevelIncrease: Int?
    val trainSkill: String?
    val assuranceForSkill: String?
    val increaseUsableSkills: Record<String, Array<String>>?
    val flags: Array<String>?
    val upgradeResults: Array<UpgradeResult>?
}

@JsModule("./feats.json")
external val kingdomFeats: Array<RawKingdomFeat>

@JsModule("./schemas/feat.json")
external val kingdomFeatSchema: JsonElement