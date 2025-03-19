package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.push
import com.foundryvtt.core.Game
import com.foundryvtt.core.Hooks
import com.foundryvtt.core.applications.api.ContextMenuEntry
import com.foundryvtt.core.game
import com.foundryvtt.core.onGetChatLogEntryContext
import io.kvision.jquery.JQuery
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

private fun Element.upgradeMeta() =
    querySelector(".km-upgrade-result")
        ?.takeIfInstance<HTMLElement>()

private fun Element.upgradeDegree() =
    upgradeMeta()
        ?.dataset["degree"]

private fun Element.isKingdomRoll() = rollMeta() != null

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
        callback = { game, elem -> buildPromise { reRoll(elem, ReRollMode.FAME_OR_INFAMY) } }
    ),
    ContextEntry(
        name = "Re-Roll Using Creative Solution",
        condition = { game, elem ->
            val creativeSolution = elem.findKingdomActor(game)
                ?.let { it.getKingdom()?.creativeSolutions }
                ?: 0
            creativeSolution > 0 && elem.isKingdomRoll()
        },
        callback = { game, elem -> buildPromise { reRoll(elem, ReRollMode.CREATIVE_SOLUTION) } }
    ),
    ContextEntry(
        name = "Re-Roll",
        condition = { game, elem -> elem.findKingdomActor(game) != null && elem.isKingdomRoll() },
        callback = { game, elem -> buildPromise { reRoll(elem, ReRollMode.DEFAULT) } }
    ),
    ContextEntry(
        name = "Re-Roll Keep Higher",
        condition = { game, elem -> elem.findKingdomActor(game) != null && elem.isKingdomRoll() },
        callback = { game, elem -> buildPromise { reRoll(elem, ReRollMode.ROLL_TWICE_KEEP_HIGHEST) } }
    ),
    ContextEntry(
        name = "Re-Roll Keep Lower",
        condition = { game, elem -> elem.findKingdomActor(game) != null && elem.isKingdomRoll() },
        callback = { game, elem -> buildPromise { reRoll(elem, ReRollMode.ROLL_TWICE_KEEP_LOWEST) } }
    ),
    ContextEntry(
        name = "Upgrade Degree of Success",
        icon = "<i class=\"fa-solid fa-arrow-up\"></i>",
        condition = { game, elem -> elem.findKingdomActor(game) != null && elem.canUpgrade() },
        callback = { game, elem ->
            buildPromise {
                elem.upgradeMeta()?.let {
                    changeDegree(it, mode = ChangeDegree.UPGRADE)
                }
            }
        }
    ),
    ContextEntry(
        name = "Downgrade Degree of Success",
        icon = "<i class=\"fa-solid fa-arrow-down\"></i>",
        condition = { game, elem -> elem.findKingdomActor(game) != null && elem.canDowngrade() },
        callback = { game, elem ->
            buildPromise {
                elem.upgradeMeta()?.let {
                    changeDegree(it, mode = ChangeDegree.DOWNGRADE)
                }
            }
        }
    ),
)


fun registerContextMenus() {
    Hooks.onGetChatLogEntryContext { elem, items ->
        entries.forEach { but ->
            items.push(
                ContextMenuEntry(
                    name = but.name,
                    condition = { elem: JQuery ->
                        elem[0]?.let { html -> but.condition(game, html) } == true
                    },
                    icon = but.icon,
                    callback = { elem -> elem[0]?.let { html -> but.callback(game, html) } },
                ).asDynamic()
            )
        }
        undefined
    }
}