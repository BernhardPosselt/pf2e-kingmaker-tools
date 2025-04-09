package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.events.KingdomEventTrait
import at.posselt.pfrpg2e.kingdom.dialogs.pickEventSettlement
import at.posselt.pfrpg2e.kingdom.dialogs.pickLeader
import at.posselt.pfrpg2e.kingdom.sheet.executeResourceButton
import at.posselt.pfrpg2e.kingdom.structures.validateUsingSchema
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.deserializeB64Json
import at.posselt.pfrpg2e.utils.postChatMessage
import at.posselt.pfrpg2e.utils.postChatTemplate
import com.foundryvtt.core.Game
import com.foundryvtt.core.Hooks
import com.foundryvtt.core.onRenderChatLog
import com.foundryvtt.core.ui
import io.github.uuidjs.uuid.v4
import js.array.tupleOf
import js.objects.JsPlainObject
import kotlinx.browser.document
import kotlinx.html.org.w3c.dom.events.Event
import kotlinx.serialization.json.Json.Default.parseToJsonElement
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import kotlin.collections.plus

private data class ChatButton(
    val buttonClass: String,
    val callback: suspend (game: Game, actor: KingdomActor, event: Event, button: HTMLButtonElement) -> Unit,
)

@Suppress("unused")
@JsPlainObject
private external interface PayStructureContext {
    val rp: Int
    val lumber: Int
    val luxuries: Int
    val stone: Int
    val ore: Int
}

private val buttons = listOf(
    ChatButton("km-pay-structure") { game, actor, event, button ->
        val rp = button.dataset["rp"]?.toInt() ?: 0
        val lumber = button.dataset["lumber"]?.toInt() ?: 0
        val luxuries = button.dataset["luxuries"]?.toInt() ?: 0
        val stone = button.dataset["stone"]?.toInt() ?: 0
        val ore = button.dataset["ore"]?.toInt() ?: 0
        actor.getKingdom()?.let { kingdom ->
            kingdom.commodities.now.luxuries = (kingdom.commodities.now.luxuries - luxuries).coerceIn(0, Int.MAX_VALUE)
            kingdom.commodities.now.lumber = (kingdom.commodities.now.lumber - lumber).coerceIn(0, Int.MAX_VALUE)
            kingdom.commodities.now.stone = (kingdom.commodities.now.stone - stone).coerceIn(0, Int.MAX_VALUE)
            kingdom.commodities.now.ore = (kingdom.commodities.now.ore - ore).coerceIn(0, Int.MAX_VALUE)
            kingdom.resourcePoints.now = (kingdom.resourcePoints.now - rp).coerceIn(0, Int.MAX_VALUE)
            actor.setKingdom(kingdom)
            postChatTemplate(
                templatePath = "chatmessages/paid-structure.hbs",
                templateContext = PayStructureContext(
                    rp = rp,
                    lumber = lumber,
                    luxuries = luxuries,
                    stone = stone,
                    ore = ore,
                )
            )
        }
    },
    ChatButton("km-gain-lose") { game, actor, event, button ->
        val activityId = button.closest(".chat-message")
            ?.querySelector(".km-upgrade-result")
            ?.takeIfInstance<HTMLElement>()
            ?.dataset["activityId"]
        actor.getKingdom()?.let { kingdom ->
            executeResourceButton(
                game = game,
                actor = actor,
                kingdom = kingdom,
                elem = button,
                activityId = activityId,
            )
        }

    },
    ChatButton("km-gain-fame-button") { game, actor, event, button ->
        actor.getKingdom()?.let { kingdom ->
            kingdom.fame.now = (kingdom.fame.now + 1).coerceIn(0, kingdom.settings.maximumFamePoints)
            postChatMessage("Gaining 1 Fame")
            actor.setKingdom(kingdom)
        }
    },
    ChatButton("km-resolve-event") { game, actor, event, button ->
        actor.getKingdom()?.let { kingdom ->
            val eventIndex = button.dataset["eventIndex"]?.toInt()!!
            val eventId = button.dataset["eventId"]!!
            val event = kingdom.getOngoingEvents()
                .getOrNull(eventIndex)
                ?.takeIf { it.event.id == eventId }
            checkNotNull(event) {
                "Could not find event with index $eventIndex"
            }
            postChatMessage("Resolved event ${event.event.name}")
            kingdom.ongoingEvents = kingdom.ongoingEvents
                .filterIndexed { index, _ -> index != eventIndex }
                .toTypedArray()
            actor.setKingdom(kingdom)
        }
    },
    ChatButton("km-add-ongoing-event") { game, actor, event, button ->
        val id = button.dataset["eventId"]
        if (id != null) {
            actor.getKingdom()?.let { kingdom ->
                val event = kingdom.getEvent(id)
                if (event != null) {
                    val ongoingEvent = if (KingdomEventTrait.SETTLEMENT.value in event.traits) {
                        val settlements = kingdom.getAllSettlements(game).allSettlements
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
                    kingdom.ongoingEvents = kingdom.ongoingEvents + ongoingEvent
                    actor.setKingdom(kingdom)
                }
            }
        }
    },
    ChatButton("km-apply-modifier-effect") { game, actor, event, button ->
        val mod = deserializeB64Json<RawModifier>(button.dataset["data"] ?: "")
        val jsonMod = JSON.stringify(mod)
        val results = validateUsingSchema(parsedModifierSchema, parseToJsonElement(jsonMod))
        if (results.isNotEmpty()) {
            ui.notifications.error("Failed to validate modifier $jsonMod")
            ui.notifications.error("Check console log for exact errors")
            console.error(results.toTypedArray())
        } else {
            val parsedMod = if (mod.name == "Focused Attention") {
                val leader = pickLeader()
                mod.copy(applyIf = mod.applyIf.orEmpty() + RawEq(eq = tupleOf("@leader", leader.value)))
            } else {
                mod
            }.copy(id = v4())
            actor.getKingdom()?.let { kingdom ->
                kingdom.modifiers = kingdom.modifiers + parsedMod
                actor.setKingdom(kingdom)
                postChatMessage("Added modifier ${parsedMod.buttonLabel}")
            }
        }
    }
)

fun bindChatButtons(game: Game) {
    Hooks.onRenderChatLog { application, html, data ->
        val chatLog = document.getElementById("chat-log")
        chatLog?.addEventListener("click", { ev ->
            buttons.forEach { data ->
                val target = ev.target
                if (target is HTMLButtonElement && target.classList.contains(data.buttonClass)) {
                    buildPromise {
                        target.closest(".chat-message")
                            ?.findKingdomActor(game)
                            ?.let { data.callback(game, it, ev, target) }
                    }
                }
            }
        })
    }
}