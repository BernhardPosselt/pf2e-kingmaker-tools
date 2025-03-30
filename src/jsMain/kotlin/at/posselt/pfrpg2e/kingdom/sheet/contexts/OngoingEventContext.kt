package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.kingdom.OngoingEvent
import at.posselt.pfrpg2e.kingdom.SettlementResult
import at.posselt.pfrpg2e.utils.formatAsModifier
import com.foundryvtt.core.ui.enrichHtml
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.js.JsPlainObject

@Suppress("unused")
@JsPlainObject
external interface OngoingEventStageContext {
    val index: Int
    val active: Boolean
    val label: Int
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
    val open: Boolean
    var skills: Array<String>
    var leader: String
    var criticalSuccess: String
    var success: String
    var failure: String
    var criticalFailure: String
    var hideStageButton: Boolean
    var settlement: String?
}

suspend fun List<OngoingEvent>.toContext(
    openedDetails: Set<String>,
    isGM: Boolean,
    settlements: SettlementResult,
): Array<OngoingEventContext> = coroutineScope {
    mapIndexed { index, it ->
        async {
            val stage = it.currentStage
            val criticalSuccess = enrichHtml(stage.criticalSuccess?.msg ?: "")
            val success = enrichHtml(stage.success?.msg ?: "")
            val failure = enrichHtml(stage.failure?.msg ?: "")
            val criticalFailure = enrichHtml(stage.criticalFailure?.msg ?: "")
            val description = enrichHtml(it.event.description)
            val settlement = settlements.allSettlements
                .find { s -> s.id == it.settlementSceneId && (!it.secretLocation || isGM) }
                ?.name
            OngoingEventContext(
                id = "${it.event.id}-$index",
                label = it.event.name,
                description = description,
                special = it.event.special,
                resolution = it.event.resolution,
                modifier = it.event.modifier.formatAsModifier(),
                traits = it.event.traits.map { it.label }.toTypedArray(),
                location = it.event.location,
                hideStageButton = it.event.stages.size < 2 || !isGM,
                stages = it.event.stages
                    .mapIndexed { index, _ ->
                        OngoingEventStageContext(
                            index = index,
                            active = index == it.stageIndex,
                            label = index + 1,
                        )
                    }
                    .toTypedArray(),
                skills = stage.skills.map { it.label }.toTypedArray(),
                leader = stage.leader.label,
                criticalSuccess = criticalSuccess,
                success = success,
                failure = failure,
                criticalFailure = criticalFailure,
                open = ("event-${it.event.id}-$index") in openedDetails,
                settlement = settlement,
            )
        }
    }
        .awaitAll()
        .toTypedArray()
}