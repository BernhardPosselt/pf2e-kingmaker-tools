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
    val skills: Array<String>
    val leader: String
    val criticalSuccess: RawKingdomEventOutcome?
    val success: RawKingdomEventOutcome?
    val failure: RawKingdomEventOutcome?
    val criticalFailure: RawKingdomEventOutcome?
}


@JsPlainObject
external interface RawKingdomEventOutcome {
    val msg: String
    val modifiers: Array<RawModifier>?
}

@JsPlainObject
external interface RawKingdomEvent {
    val id: String
    val name: String
    val description: String
    val special: String?
    val modifiers: Array<RawModifier>?
    val resolution: String?
    val resolvedOn: Array<String>?
    val modifier: Int?
    val traits: Array<String>
    val location: String?
    val stages: Array<RawKingdomEventStage>
    val kingmakerJournalUuid: String?
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
        traits = resolvedOn?.mapNotNull { KingdomEventTrait.fromString(it) }?.toSet().orEmpty(),
        location = location,
        stages = stages.map { it.parse() },
        kingmakerJournalUuid = kingmakerJournalUuid,
    )

@JsPlainObject
external interface RawActiveKingdomEvent {
    val stage: Int
    val event: RawKingdomEvent
}

fun KingdomData.getEvents(): Array<RawKingdomEvent> {
    val overrides = homebrewKingdomEvents.map { it.id }.toSet()
    return homebrewKingdomEvents + kingdomEvents.filter { it.id !in overrides }
}

fun KingdomData.getEvent(id: String): RawKingdomEvent? =
    getEvents().associateBy { it.id }[id]