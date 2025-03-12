package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.kingdom.modifiers.DowngradeResult
import at.posselt.pfrpg2e.kingdom.modifiers.UpgradeResult
import at.posselt.pfrpg2e.utils.asSequence
import js.objects.JsPlainObject
import js.objects.Record
import kotlinx.serialization.json.JsonElement


@JsPlainObject
external interface RawUpgradeResult {
    val upgrade: String
    val times: Int?
}


fun RawUpgradeResult.parse() =
    DegreeOfSuccess.fromString(upgrade)?.let { degree ->
        UpgradeResult(
            upgrade = degree,
            times = times ?: 1
        )
    }

@JsPlainObject
external interface RawDowngradeResult {
    val downgrade: String
    val times: Int?
}


fun RawDowngradeResult.parse() =
    DegreeOfSuccess.fromString(downgrade)?.let { degree ->
        DowngradeResult(
            downgrade = degree,
            times = times ?: 1,
        )
    }

@JsPlainObject
external interface RawRuinThresholdIncreases {
    val amount: Int
    val increase: Int
}


@JsPlainObject
external interface RawFeat {
    val id: String
    val name: String
    val level: Int
    val text: String
    val prerequisites: String?
    val automationNotes: String?
    val modifiers: Array<RawModifier>?
    val resourceDice: Int?
    val settlementMagicItemLevelIncrease: Int?
    val trainSkill: String?
    val assuranceForSkill: String?
    val increaseUsableSkills: Record<String, Array<String>>?
    val flags: Array<String>?
    val increaseAnarchyLimit: Int?
    val ruinThresholdIncreases: Array<RawRuinThresholdIncreases>?
}

fun RawFeat.increasedSkills(): Map<KingdomSkill, Set<KingdomSkill>> =
    increaseUsableSkills?.asSequence()
        ?.mapNotNull { (skill, skills) ->
            KingdomSkill.fromString(skill)?.let { kingdomSkill ->
                kingdomSkill to skills.mapNotNull { s -> KingdomSkill.fromString(s) }.toSet()
            }
        }
        ?.toMap()
        ?: emptyMap()

fun KingdomData.getFeats(): Array<RawFeat> {
    val overrides = homebrewFeats.map { it.id }.toSet()
    return homebrewFeats + kingdomFeats.filter { it.id !in overrides }
}

@JsModule("./feats.json")
external val kingdomFeats: Array<RawFeat>

@JsModule("./schemas/feat.json")
external val kingdomFeatSchema: JsonElement