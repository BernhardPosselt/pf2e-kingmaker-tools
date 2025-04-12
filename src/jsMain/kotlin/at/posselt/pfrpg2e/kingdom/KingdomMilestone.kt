package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.kingdom.data.MilestoneChoice
import at.posselt.pfrpg2e.utils.t
import kotlinx.js.JsPlainObject

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
private external val kingdomMilestones: Array<RawMilestone>

private fun RawMilestone.translate() =
    copy(
        name = t(name),
    )

private var translatedMilestones = emptyArray<RawMilestone>()

fun translateMilestones() {
    translatedMilestones = kingdomMilestones
        .map { it.translate() }
        .toTypedArray()
}

val initialMilestoneChoices = kingdomMilestones
    .map {
        MilestoneChoice(
            id = it.id,
            completed = false,
            enabled = it.enabledOnFirstRun,
        )
    }
    .toTypedArray()

fun KingdomData.getMilestones(): Array<RawMilestone> {
    val overrides = homebrewMilestones.map { it.id }.toSet()
    return homebrewMilestones + translatedMilestones.filter { it.id !in overrides }
}