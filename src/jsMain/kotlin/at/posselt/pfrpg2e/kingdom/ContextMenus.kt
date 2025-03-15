package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.buildUuid
import at.posselt.pfrpg2e.utils.push
import com.foundryvtt.core.Game
import com.foundryvtt.core.Hooks
import com.foundryvtt.core.applications.api.ContextMenuEntry
import com.foundryvtt.core.onGetChatLogEntryContext
import io.kvision.jquery.get
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

fun Element.findKingdomActor(game: Game) =
    querySelector("[data-kingdom-actor-uuid]")
        ?.takeIfInstance<HTMLElement>()
        ?.dataset["kingdomActorUuid"]
        ?.let { uuid -> game.actors.find { it.uuid == uuid } }
        ?.takeIfInstance<KingdomActor>()
        ?.takeIf { it.getKingdom() != null }

private fun Element.rollMeta() =
    querySelector(".km-roll-meta")
        ?.takeIfInstance<HTMLElement>()

private fun Element.contentLink() =
    querySelector(".content-link")
        ?.takeIfInstance<HTMLElement>()

private fun Element.upgradeDegree() =
    querySelector(".km-upgrade-result")
        ?.takeIfInstance<HTMLElement>()
        ?.dataset["degree"]

private fun Element.isKingdomRoll() = rollMeta() != null

private fun Element.isKingdomRollResult() = upgradeDegree() != null

private fun Element.canUpgrade(): Boolean {
    val degree = upgradeDegree()
    return degree != null && DegreeOfSuccess.fromString(degree) != DegreeOfSuccess.CRITICAL_SUCCESS
}

private fun Element.canDowngrade(): Boolean {
    val degree = upgradeDegree()
    return degree != null && DegreeOfSuccess.fromString(degree) != DegreeOfSuccess.CRITICAL_FAILURE
}

private data class ContextEntry(
    val name: String,
    val icon: String = "<i class=\"fa-solid fa-dice-d20\"></i>",
    val condition: (game: Game, HTMLElement) -> Boolean,
    val callback: (game: Game, HTMLElement) -> Unit,
)

private val entries = listOf<ContextEntry>(
    ContextEntry(
        name = "Re-Roll Using Fame/Infamy",
        condition = { game, elem ->
            val fame = elem.findKingdomActor(game)
                ?.let { it.getKingdom()?.fame?.now }
                ?: 0
            fame > 0 && elem.isKingdomRoll()
        },
        callback = { game, elem -> console.log(elem) } // TODO: re-roll
    ),
    ContextEntry(
        name = "Re-Roll Using Creative Solution",
        condition = { game, elem ->
            val creativeSolution = elem.findKingdomActor(game)
                ?.let { it.getKingdom()?.creativeSolutions }
                ?: 0
            creativeSolution > 0 && elem.isKingdomRoll()
        },
        callback = { game, elem -> console.log(elem) } // TODO: re-roll using creative solution
    ),
    ContextEntry(
        name = "Re-Roll Keep Higher",
        condition = { game, elem -> elem.findKingdomActor(game) != null && elem.isKingdomRoll() },
        callback = { game, elem -> console.log(elem) } // TODO: re-roll keep higher
    ),
    ContextEntry(
        name = "Re-Roll Keep Lower",
        condition = { game, elem -> elem.findKingdomActor(game) != null && elem.isKingdomRoll() },
        callback = { game, elem -> console.log(elem) } // TODO: re-roll keep lower
    ),
    ContextEntry(
        name = "Upgrade Degree of Success",
        icon = "<i class=\"fa-solid fa-arrow-up\"></i>",
        condition = { game, elem -> elem.findKingdomActor(game) != null && elem.canUpgrade() },
        callback = { game, elem -> console.log(elem) } // TODO: upgrade
    ),
    ContextEntry(
        name = "Downgrade Degree of Success",
        icon = "<i class=\"fa-solid fa-arrow-down\"></i>",
        condition = { game, elem -> elem.findKingdomActor(game) != null && elem.canDowngrade() },
        callback = { game, elem -> console.log(elem) } // TODO: downgrade
    ),
    ContextEntry(
        name = "Add to Ongoing Events",
        icon = "<i class=\"fa-solid fa-plus\"></i>",
        condition = { game, elem -> elem.findKingdomActor(game) != null && elem.contentLink() != null },
        callback = { game, elem ->
            val actor = elem.findKingdomActor(game)
            actor?.getKingdom()?.let { kingdom ->
                buildPromise {
                    val link = elem.contentLink()
                    val uuid = link?.dataset["uuid"]
                    val text = link?.innerText
                    if (uuid != null && text != null) {
                        val event = buildUuid(uuid, text)
                        kingdom.ongoingEvents = kingdom.ongoingEvents + OngoingEvent(name=event)
                        actor.setKingdom(kingdom)
                    }
                }
            }
        }
    ),
)

fun registerContextMenus(game: Game) {
    Hooks.onGetChatLogEntryContext { elem, items ->
        entries.forEach {
            items.push(
                ContextMenuEntry(
                    name = it.name,
                    condition = { elem -> elem[0]?.let { html -> it.condition(game, html) } ?: false },
                    icon = it.icon,
                    callback = { elem -> elem[0]?.let { html -> it.callback(game, html) } },
                )
            )
        }
    }
}