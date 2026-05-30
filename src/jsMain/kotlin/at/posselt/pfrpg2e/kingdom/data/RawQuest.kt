package at.posselt.pfrpg2e.kingdom.data

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RawQuestRewards {
    var rp: Int?
    var xp: Int?
    var unrest: Int?
    var food: Int?
    var lumber: Int?
    var stone: Int?
    var ore: Int?
    var luxuries: Int?
}

@JsPlainObject
external interface RawQuest {
    var id: String
    var title: String
    var description: String
    var giver: String
    var status: String // "active" | "completed"
    var type: String   // "explore_hex" | "claim_hex" | "build_structure" | "clear_hex" | "assign_leader" | "other"
    var target: String?
    var rewards: RawQuestRewards
    var flavorTextCompleted: String
}
