package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.SearchInput
import at.posselt.pfrpg2e.data.events.KingdomEventTrait
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.RawOngoingKingdomEvent
import at.posselt.pfrpg2e.kingdom.getEvents
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.sheet.executeResourceButton
import at.posselt.pfrpg2e.toLabel
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.formatAsModifier
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.applications.api.ApplicationRenderOptions
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.core.ui.enrichHtml
import js.core.Void
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.awaitAll
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

@JsPlainObject
external interface AddEventStagesContext {
    var skills: Array<String>
    var leader: String
    var criticalSuccess: String
    var success: String
    var failure: String
    var criticalFailure: String
}

@JsPlainObject
external interface AddEventContext {
    var id: String
    var label: String
    var description: String
    var special: String?
    var resolution: String?
    var traits: Array<String>
    var location: String?
    var stages: Array<AddEventStagesContext>
    var isSettlement: Boolean
}


@JsPlainObject
external interface AddEventsContext : ValidatedHandlebarsContext {
    var events: Array<AddEventContext>
    var search: FormElementContext
}

@JsPlainObject
external interface AddEventsData {
    val search: String
}

@JsExport
class AddEventsDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            string("search")
        }
    }
}

class AddEvent(
    private val game: Game,
    private val kingdomActor: KingdomActor,
    private val kingdom: KingdomData,
    private val settlements: List<Settlement>,
    private val onSave: suspend (event: RawOngoingKingdomEvent) -> Unit,
) : FormApp<AddEventsContext, AddEventsData>(
    title = "Add Event",
    template = "applications/kingdom/event-browser.hbs",
    width = 600,
    id = "kmEvents-${kingdomActor.uuid}",
    dataModel = AddEventsDataModel::class.js,
    scrollable = arrayOf(".km-add-events"),
) {
    var search = ""

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "add-event" -> {
                buildPromise {
                    val id = target.dataset["id"] as String
                    val isSettlementEvent = target.dataset["settlementEvent"] == "true"
                    val event = if (isSettlementEvent) {
                        val pick = pickEventSettlement(settlements)
                        RawOngoingKingdomEvent(
                            stage = 0,
                            id = id,
                            settlementSceneId = pick.settlementId,
                            secretLocation = pick.secretLocation,
                        )
                    } else {
                        RawOngoingKingdomEvent(
                            stage = 0,
                            id = id,
                        )
                    }
                    onSave(event)
                    close()
                }
            }
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<AddEventsContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val events = kingdom.getEvents(applyBlacklist = true)
            .sortedBy { it.name }
            .filter {
                val stages = it.stages.joinToString(" ") {
                    (it.criticalSuccess?.msg ?: "") +
                            (it.success?.msg ?: "") +
                            (it.failure?.msg ?: "") +
                            (it.criticalFailure?.msg ?: "")
                }
                val haystack =
                    "${it.name} ${it.resolution ?: ""} ${it.special ?: ""} ${it.location ?: ""} $stages".lowercase()
                search.split(" ").all { haystack.contains(it) }
            }
            .map {
                async {
                    val modifier = it.modifier
                    val stages = it.stages.map { stage ->
                        val criticalSuccess = enrichHtml(stage.criticalSuccess?.msg ?: "")
                        val success = enrichHtml(stage.success?.msg ?: "")
                        val failure = enrichHtml(stage.failure?.msg ?: "")
                        val criticalFailure = enrichHtml(stage.criticalFailure?.msg ?: "")
                        AddEventStagesContext(
                            skills = stage.skills.map { it.toLabel() }.toTypedArray(),
                            leader = stage.leader.toLabel(),
                            criticalSuccess = criticalSuccess,
                            success = success,
                            failure = failure,
                            criticalFailure = criticalFailure,
                        )
                    }.toTypedArray()
                    val description = enrichHtml(it.description)
                    AddEventContext(
                        id = it.id,
                        label = it.name + if (modifier != null && modifier != 0) " (${modifier.formatAsModifier()})" else "",
                        description = description,
                        special = it.special,
                        resolution = it.resolution,
                        traits = it.traits.map { it.toLabel() }.toTypedArray(),
                        location = it.location,
                        stages = stages,
                        isSettlement = KingdomEventTrait.SETTLEMENT.value in it.traits,
                    )
                }
            }
            .awaitAll()
            .toTypedArray()
        AddEventsContext(
            partId = parent.partId,
            events = events,
            isFormValid = isFormValid,
            search = SearchInput(
                name = "search",
                label = "Filter",
                hideLabel = true,
                value = search,
                placeholder = "Search",
                required = false,
            ).toContext()
        )
    }

    override fun _attachPartListeners(partId: String, htmlElement: HTMLElement, options: ApplicationRenderOptions) {
        super._attachPartListeners(partId, htmlElement, options)
        htmlElement.querySelectorAll(".km-gain-lose").asList()
            .filterIsInstance<HTMLElement>()
            .forEach { elem ->
                elem.addEventListener("click", {
                    kingdomActor.getKingdom()?.let { kingdom ->
                        buildPromise {
                            executeResourceButton(
                                game = game,
                                actor = kingdomActor,
                                kingdom = kingdom,
                                elem = elem,
                            )
                        }
                    }
                })
            }
    }

    override fun onParsedSubmit(value: AddEventsData): Promise<Void> = buildPromise {
        search = value.search
        undefined
    }
}