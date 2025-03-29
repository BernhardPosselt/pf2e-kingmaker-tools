package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.data.events.KingdomEventStage
import at.posselt.pfrpg2e.kingdom.OngoingEvent
import at.posselt.pfrpg2e.utils.formatAsModifier
import kotlinx.js.JsPlainObject

@Suppress("unused")
@JsPlainObject
external interface OngoingEventStageContext {
    var skills: Array<String>
    var leader: String
    var criticalSuccess: String
    var success: String
    var failure: String
    var criticalFailure: String
}


@Suppress("unused")
@JsPlainObject
external interface OngoingEventContext {
    var id: String
    var label: String
    var description: String
    var special: String?
    var resolution: String?
    var modifier: String
    var traits: Array<String>
    var location: String?
    var stages: Array<OngoingEventStageContext>
    var currentStage: Int
    val open: Boolean
}

fun KingdomEventStage.toContext() =
    OngoingEventStageContext(
        skills = skills.map { it.label }.toTypedArray(),
        leader = leader.label,
        criticalSuccess = criticalSuccess?.msg ?: "",
        success =success?.msg ?: "",
        failure =failure?.msg ?: "",
        criticalFailure =criticalFailure?.msg ?: "",
    )

fun List<OngoingEvent>.toContext(openedDetails: Set<String>): Array<OngoingEventContext> =
    mapIndexed { index, it ->
        OngoingEventContext(
            id = "${it.event.id}-$index",
            label = it.event.name,
            description = it.event.description,
            special = it.event.special,
            resolution = it.event.resolution,
            modifier = it.event.modifier.formatAsModifier(),
            traits = it.event.traits.map { it.label }.toTypedArray(),
            location = it.event.location,
            stages = it.event.stages.map { stage -> stage.toContext() }.toTypedArray(),
            currentStage = it.stageIndex,
            open = ("event-${it.event.id}-$index") in openedDetails,
        )
    }.toTypedArray()