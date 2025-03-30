package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.events.KingdomEvent
import at.posselt.pfrpg2e.data.events.KingdomEventOutcome
import at.posselt.pfrpg2e.data.events.KingdomEventStage
import at.posselt.pfrpg2e.data.events.KingdomEventTrait
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import js.objects.JsPlainObject
import kotlinx.serialization.json.JsonElement

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
    var resolution: String?
    var resolvedOn: Array<String>?
    var modifier: Int?
    var traits: Array<String>
    var location: String?
    var stages: Array<RawKingdomEventStage>
}


@JsModule("./events.json")
external val kingdomEvents: Array<RawKingdomEvent>

@Suppress("unused")
@JsModule("./schemas/event.json")
external val kingdomEventSchema: JsonElement

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

fun KingdomData.getEvents(applyBlacklist: Boolean = false): Array<RawKingdomEvent> {
    val overrides = homebrewKingdomEvents.map { it.id }.toSet()
    val result = homebrewKingdomEvents + kingdomEvents.filter { it.id !in overrides }
    return if (applyBlacklist) {
        result.filter { it.id !in kingdomEventBlacklist }.toTypedArray()
    } else {
        result
    }
}

fun KingdomData.getParsedEvents(applyBlacklist: Boolean = false): List<KingdomEvent> {
    val overrides = homebrewKingdomEvents.map { it.id }.toSet()
    val result = (homebrewKingdomEvents + kingdomEvents.filter { it.id !in overrides }).map { it.parse() }
    return if (applyBlacklist) {
        result.filter { it.id !in kingdomEventBlacklist }
    } else {
        result
    }
}

fun KingdomData.getEvent(id: String): RawKingdomEvent? =
    getEvents().associateBy { it.id }[id]