package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRank
import at.posselt.pfrpg2e.data.kingdom.RealmData
import at.posselt.pfrpg2e.data.kingdom.calculateControlDC
import at.posselt.pfrpg2e.utils.asSequence
import js.objects.JsPlainObject
import kotlinx.serialization.json.JsonElement

typealias KingdomDc = Any // number or control, custom, none, scouting

@JsPlainObject
external interface ActivityResult {
    var msg: String
    val modifiers: Array<RawModifier>
}

@JsPlainObject
external interface ActivityResults {
    var criticalSuccess: ActivityResult?
    var success: ActivityResult?
    var failure: ActivityResult?
    var criticalFailure: ActivityResult?
}

@JsPlainObject
external interface KingdomActivity {
    var id: String
    var title: String
    var description: String
    var requirement: String?
    var special: String?
    var skills: RawSkillRanks
    var phase: KingdomPhase
    var dc: KingdomDc
    var dcAdjustment: Int?
    var enabled: Boolean
    var companion: Companion?
    var fortune: Boolean
    var oncePerRound: Boolean
    var hint: String?
}

@JsModule("./kingdom-activities.json")
external val kingdomActivities: Array<KingdomActivity>

@JsModule("./schemas/kingdom-activity.json")
external val kingdomActivitySchema: JsonElement

fun KingdomActivity.resolveDc(
    enemyArmyScoutingDcs: List<Int>,
    kingdomLevel: Int,
    realm: RealmData,
    rulerVacant: Boolean,
): Int? =
    when (dc) {
        "control" -> calculateControlDC(
            kingdomLevel = kingdomLevel,
            realm = realm,
            rulerVacant = rulerVacant,
        )
        "custom" -> 0
        "none" -> null
        "scouting" -> enemyArmyScoutingDcs.maxOrNull() ?: 0
        else -> dc as Int
    }

fun KingdomActivity.skillRanks(): Set<KingdomSkillRank> =
    skills.asSequence()
        .mapNotNull { (name, rank) ->
            KingdomSkill.fromString(name)?.let {
                KingdomSkillRank(skill = it, rank = rank)
            }
        }
        .toSet()