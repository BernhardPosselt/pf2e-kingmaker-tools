package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.kingdom.data.getChosenFeats
import at.posselt.pfrpg2e.kingdom.data.getChosenFeatures
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.push
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.Game
import com.foundryvtt.core.applications.api.ContextMenuEntry
import com.foundryvtt.core.game
import com.foundryvtt.core.helpers.TypedHooks
import com.foundryvtt.core.helpers.onGetChatMessageContextOptions
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
        name = t("kingdom.rerollUsingFame"),
        condition = { game, elem ->
            val fame = elem.findKingdomActor(game)
                ?.let { it.getKingdom()?.fame?.now }
                ?: 0
            fame > 0 && elem.isKingdomRoll()
        },
        callback = { game, elem -> buildPromise { reRoll(elem, ReRollMode.FAME_OR_INFAMY) } }
    ),
    ContextEntry(
        name = t("kingdom.rerollUsingSolution"),
        condition = { game, elem ->
            val creativeSolution = elem.findKingdomActor(game)
                ?.let { it.getKingdom()?.creativeSolutions }
                ?: 0
            creativeSolution > 0 && elem.isKingdomRoll()
        },
        callback = { game, elem -> buildPromise { reRoll(elem, ReRollMode.CREATIVE_SOLUTION) } }
    ),
    ContextEntry(
        name = t("kingdom.reroll"),
        condition = { game, elem -> elem.findKingdomActor(game) != null && elem.isKingdomRoll() },
        callback = { game, elem -> buildPromise { reRoll(elem, ReRollMode.DEFAULT) } }
    ),
    ContextEntry(
        name = t("kingdom.rerollKH"),
        condition = { game, elem -> elem.findKingdomActor(game) != null && elem.isKingdomRoll() },
        callback = { game, elem -> buildPromise { reRoll(elem, ReRollMode.ROLL_TWICE_KEEP_HIGHEST) } }
    ),
    ContextEntry(
        name = t("kingdom.rerollKL"),
        condition = { game, elem -> elem.findKingdomActor(game) != null && elem.isKingdomRoll() },
        callback = { game, elem -> buildPromise { reRoll(elem, ReRollMode.ROLL_TWICE_KEEP_LOWEST) } }
    ),
    ContextEntry(
        name = t("kingdom.rerollUsingRp"),
        condition = { game, elem ->
            val rollMeta = elem.rollMeta()
            val activity = rollMeta?.dataset["activityId"]
            val degree = rollMeta
                ?.dataset["degree"]
                ?.let { DegreeOfSuccess.fromString(it) }
            elem.findKingdomActor(game)
                ?.getKingdom()
                ?.let { kingdom ->
                    val features = kingdom.getChosenFeatures(kingdom.getExplodedFeatures())
                    val hasTwoRp = kingdom.resourcePoints.now >= 2
                    val hasFreeAndFair = kingdom.getChosenFeats(features).any { it.feat.isFreeAndFair == true }
                    val failed = degree?.failed() == true
                    val newLeadershipOrPledgeOfFealty =
                        activity in setOf("new-leadership", "new-leadership-vk", "pledge-of-fealty")
                    hasFreeAndFair && hasTwoRp && failed && newLeadershipOrPledgeOfFealty
                } == true
        },
        callback = { game, elem -> buildPromise { reRoll(elem, ReRollMode.FREE_AND_FAIR) } }
    ),
    ContextEntry(
        name = t("kingdom.upgradeDegree"),
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
        name = t("kingdom.downgradeDegree"),
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
    TypedHooks.onGetChatMessageContextOptions { elem, items ->
        entries.forEach { but ->
            items.push(
                ContextMenuEntry(
                    name = but.name,
                    condition = { elem: HTMLElement ->
                        but.condition(game, elem)
                    },
                    icon = but.icon,
                    callback = { elem -> elem.let { html -> but.callback(game, html) } },
                ).asDynamic()
            )
        }
        undefined
    }
}