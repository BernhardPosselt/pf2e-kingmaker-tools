package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.kingdom.dialogs.pickLeader
import at.posselt.pfrpg2e.kingdom.resources.calculateStorage
import at.posselt.pfrpg2e.kingdom.sheet.ResourceButton
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.buildUuid
import at.posselt.pfrpg2e.utils.deserializeB64Json
import at.posselt.pfrpg2e.utils.postChatMessage
import at.posselt.pfrpg2e.utils.postChatTemplate
import com.foundryvtt.core.Game
import com.foundryvtt.core.Hooks
import com.foundryvtt.core.onRenderChatLog
import io.github.uuidjs.uuid.v4
import js.array.tupleOf
import js.objects.JsPlainObject
import kotlinx.browser.document
import kotlinx.html.org.w3c.dom.events.Event
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.get

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
        val resourceButton = ResourceButton.fromHtml(button)
        actor.getKingdom()?.let { kingdom ->
            val realm = game.getRealmData(actor, kingdom)
            val settlements = kingdom.getAllSettlements(game)
            val storage = calculateStorage(realm = realm, settlements = settlements.allSettlements)
            resourceButton.evaluate(
                kingdom = kingdom,
                dice = realm.sizeInfo.resourceDieSize,
                maximumFame = kingdom.settings.maximumFamePoints,
                storage = storage,
            )
            actor.setKingdom(kingdom)
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
            val event = kingdom.getOngoingEvents().getOrNull(eventIndex)
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
        val uuid = button.dataset["link"]
        if (uuid != null) {
            actor.getKingdom()?.let { kingdom ->
                val event = buildUuid(uuid)
                // TODO
//                kingdom.ongoingEvents = kingdom.ongoingEvents + OngoingEvent(name = event)
                actor.setKingdom(kingdom)
            }
        }
    },
    ChatButton("km-apply-modifier-effect") { game, actor, event, button ->
        val mod = deserializeB64Json<RawModifier>(button.dataset["data"] ?: "")
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