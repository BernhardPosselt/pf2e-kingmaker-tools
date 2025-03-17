package at.posselt.pfrpg2e.kingdom

import kotlinx.js.JsPlainObject
import kotlinx.serialization.json.JsonElement

@JsPlainObject
external interface RawMilestone {
    var id: String
    var name: String
    var xp: Int
    var completed: Boolean
    var enabledOnFirstRun: Boolean
    var isCultMilestone: Boolean
}

@JsModule("./milestones.json")
external val kingdomMilestones: Array<RawMilestone>

@Suppress("unused")
@JsModule("./schemas/milestone.json")
external val milestoneSchema: JsonElement

fun KingdomData.getMilestones(): Array<RawMilestone> {
    val overrides = homebrewMilestones.map { it.id }.toSet()
    return homebrewMilestones + kingdomMilestones.filter { it.id !in overrides }
}