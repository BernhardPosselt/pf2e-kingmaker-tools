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
    val applyIf: Array<RawExpression<Boolean>>?
}


fun RawUpgradeResult.parse() =
    DegreeOfSuccess.fromString(upgrade)?.let { degree ->
        UpgradeResult(
            upgrade = degree,
            applyIf = applyIf?.map { it.parse() } ?: emptyList()
        )
    }

@JsPlainObject
external interface RawDowngradeResult {
    val downgrade: String
    val applyIf: Array<RawExpression<Boolean>>?
}


fun RawDowngradeResult.parse() =
    DegreeOfSuccess.fromString(downgrade)?.let { degree ->
        DowngradeResult(
            downgrade = degree,
            applyIf = applyIf?.map { it.parse() } ?: emptyList()
        )
    }

@JsPlainObject
external interface RawRuinThresholdIncreases {
    val amount: Int
    val increase: Int
}


@JsPlainObject
external interface RawKingdomFeat {
    val id: String
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
    val upgradeResults: Array<RawUpgradeResult>?
    val increaseAnarchyLimit: Int?
    val ruinThresholdIncreases: Array<RawRuinThresholdIncreases>?
}

fun RawKingdomFeat.increasedSkills(): Map<KingdomSkill, Set<KingdomSkill>> =
    increaseUsableSkills?.asSequence()
        ?.mapNotNull { (skill, skills) ->
            KingdomSkill.fromString(skill)?.let { kingdomSkill ->
                kingdomSkill to skills.mapNotNull { s -> KingdomSkill.fromString(s) }.toSet()
            }
        }
        ?.toMap()
        ?: emptyMap()

fun KingdomData.getFeats(): Array<RawKingdomFeat> {
    return kingdomFeats
}

@JsModule("./feats.json")
external val kingdomFeats: Array<RawKingdomFeat>

@JsModule("./schemas/feat.json")
external val kingdomFeatSchema: JsonElement