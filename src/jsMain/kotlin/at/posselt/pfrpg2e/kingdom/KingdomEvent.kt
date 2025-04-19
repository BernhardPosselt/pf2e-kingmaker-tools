package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.events.KingdomEvent
import at.posselt.pfrpg2e.data.events.KingdomEventOutcome
import at.posselt.pfrpg2e.data.events.KingdomEventStage
import at.posselt.pfrpg2e.data.events.KingdomEventTrait
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.kingdom.sheet.insertButtons
import at.posselt.pfrpg2e.utils.t
import js.objects.JsPlainObject

@JsPlainObject
external interface RawKingdomEventStage {
    var skills: Array<String>
    var leader: String
    var criticalSuccess: RawKingdomEventOutcome?
    var success: RawKingdomEventOutcome?
    var failure: RawKingdomEventOutcome?
    var criticalFailure: RawKingdomEventOutcome?
}


@JsPlainObject
external interface RawKingdomEventOutcome {
    var msg: String
    var modifiers: Array<RawModifier>?
}

@JsPlainObject
external interface RawKingdomEvent {
    var id: String
    var name: String
    var description: String
    var special: String?
    var modifiers: Array<RawModifier>?
    var globalModifiers: Array<RawModifier>?
    var resolution: String?
    var resolvedOn: Array<String>?
    var modifier: Int?
    var traits: Array<String>
    var location: String?
    var stages: Array<RawKingdomEventStage>
    var automationNotes: String?
}


@JsModule("./events.json")
private external val kingdomEvents: Array<RawKingdomEvent>

fun RawKingdomEventOutcome.parse() =
    KingdomEventOutcome(
        msg = msg,
        modifiers = modifiers?.map { it.parse() }.orEmpty(),
    )

fun RawKingdomEventStage.parse() =
    KingdomEventStage(
        skills = skills.mapNotNull { KingdomSkill.fromString(it) }.toSet(),
        leader = Leader.fromString(leader) ?: Leader.RULER,
        criticalSuccess = criticalSuccess?.parse(),
        success = success?.parse(),
        failure = failure?.parse(),
        criticalFailure = criticalFailure?.parse(),
    )

fun RawKingdomEvent.parse() =
    KingdomEvent(
        id = id,
        name = name,
        description = description,
        special = special,
        modifiers = modifiers?.map { it.parse() }?.toList().orEmpty(),
        resolution = resolution,
        resolvedOn = resolvedOn?.mapNotNull { DegreeOfSuccess.fromString(it) }?.toSet().orEmpty(),
        modifier = modifier ?: 0,
        traits = traits.mapNotNull { KingdomEventTrait.fromString(it) }.toSet(),
        location = location,
        stages = stages.map { it.parse() },
        automationNotes = automationNotes,
        globalModifiers = globalModifiers?.map { it.parse() }?.toList().orEmpty(),
    )

@JsPlainObject
external interface RawOngoingKingdomEvent {
    val stage: Int
    val id: String
    var settlementSceneId: String?
    var secretLocation: Boolean?
}

data class OngoingEvent(
    val stageIndex: Int,
    val event: KingdomEvent,
    val eventIndex: Int,
    val secretLocation: Boolean,
    val settlementSceneId: String?,
) {
    val stageCount = event.stages.size
    val currentStage = event.stages[stageIndex]
}

fun KingdomData.getOngoingEvents(applyBlacklist: Boolean = false): List<OngoingEvent> {
    val eventsById = getEvents(applyBlacklist).associateBy { it.id }
    return ongoingEvents.mapIndexedNotNull { index, ongoing ->
        eventsById[ongoing.id]?.let { event ->
            OngoingEvent(
                stageIndex = ongoing.stage,
                event = event.parse(),
                eventIndex = index,
                secretLocation = ongoing.secretLocation == true,
                settlementSceneId = ongoing.settlementSceneId,
            )
        }
    }
}

private fun RawKingdomEventOutcome.translate() =
    copy(
        msg = insertButtons(t(msg))
    )

private fun RawKingdomEventStage.translate() =
    copy(
        criticalSuccess = criticalSuccess?.let { it.translate() },
        success = success?.let { it.translate() },
        failure = failure?.let { it.translate() },
        criticalFailure = criticalFailure?.let { it.translate() },
    )

private fun RawKingdomEvent.translate() =
    copy(
        name = t(name),
        description = insertButtons(t(description)),
        special = special?.let { t(it) },
        resolution = resolution?.let { t(it) },
        location = location?.let { t(it) },
        stages = stages.map { it.translate() }.toTypedArray(),
        automationNotes = automationNotes?.let { t(it) },
    )

private var translatedKingdomEvents = emptyArray<RawKingdomEvent>()

fun translateKingdomEvents() {
    translatedKingdomEvents = kingdomEvents
        .map { it.translate() }
        .toTypedArray()
}

fun KingdomData.getEvents(applyBlacklist: Boolean = false): Array<RawKingdomEvent> {
    val overrides = homebrewKingdomEvents.map { it.id }.toSet()
    val result = homebrewKingdomEvents + translatedKingdomEvents.filter { it.id !in overrides }
    return if (applyBlacklist) {
        result.filter { it.id !in kingdomEventBlacklist }.toTypedArray()
    } else {
        result
    }
}

fun KingdomData.getEvent(id: String): RawKingdomEvent? =
    getEvents().associateBy { it.id }[id]