package at.posselt.pfrpg2e.kingdom

import js.objects.JsPlainObject
import kotlinx.serialization.json.JsonElement

typealias KingdomDc = Any // number or control, custom, none, scouting

@JsPlainObject
external interface ActivityResult {
    var msg: String
    val modifiers: Array<ChatModifier>
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
    var skills: SkillRanks
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