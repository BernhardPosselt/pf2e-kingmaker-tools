package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.forms.SimpleApp
import at.posselt.pfrpg2e.data.checks.getLevelBasedDC
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.armies.getAllAvailableArmyTactics
import at.posselt.pfrpg2e.kingdom.armies.hasTactic
import at.posselt.pfrpg2e.kingdom.armies.isArmyTactic
import at.posselt.pfrpg2e.kingdom.getActivity
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.utils.awaitAll
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.buildUuid
import at.posselt.pfrpg2e.utils.launch
import com.foundryvtt.core.Game
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.ui.TextEditor
import com.foundryvtt.pf2e.actor.PF2EArmy
import com.foundryvtt.pf2e.actor.PF2ENpc
import com.foundryvtt.pf2e.item.PF2ECampaignFeature
import com.foundryvtt.pf2e.item.itemFromUuid
import js.objects.JsPlainObject
import kotlinx.coroutines.await
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

@JsPlainObject
private external interface ArmyTacticContext {
    val link: String
    val level: Int
    val dc: Int
    val enabled: Boolean
    val uuid: String
}


@JsPlainObject
private external interface ArmyTacticsContext : HandlebarsRenderContext {
    val tactics: Array<ArmyTacticContext>
}

private class ArmyTacticsBrowser(
    private val game: Game,
    private val kingdomActor: PF2ENpc,
    private val kingdom: KingdomData,
    private val army: PF2EArmy,
) : SimpleApp<ArmyTacticsContext>(
    title = "Learnable Tactics: ${army.name}",
    template = "applications/kingdom/army-tactics-browser.hbs",
    id = "kmArmyTactics",
    width = 600,
) {
    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "train-tactic" -> {
                buildPromise {
                    val uuid = target.dataset["uuid"] as String
                    trainTactic(uuid)
                }
            }
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<ArmyTacticsContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val tactics = game.getAllAvailableArmyTactics()
            .asSequence()
            .filter { it.system.traits.value.contains(army.system.traits.type) && !army.hasTactic(it) }
            .sortedWith(compareBy<PF2ECampaignFeature> { it.level }.thenBy { it.name })
            .map {
                buildPromise {
                    val level = it.level
                    val dc = getLevelBasedDC(level)
                    val link = TextEditor.enrichHTML(buildUuid(it.uuid, it.name)).await()
                    ArmyTacticContext(
                        link = link,
                        level = level,
                        dc = dc,
                        enabled = level <= army.level,
                        uuid = it.uuid,
                    )
                }
            }
            .toList()
            .awaitAll()
            .toTypedArray()
        ArmyTacticsContext(
            partId = parent.partId,
            tactics = tactics
        )
    }

    private suspend fun trainTactic(uuid: String) {
        val item = itemFromUuid(uuid)
            ?.takeIfInstance<PF2ECampaignFeature>()
            ?.takeIf { it.isArmyTactic }
        if (item != null) {
            val activity = this.kingdom.getActivity("train-army")
            checkNotNull(activity) {
                "Could not find train-army activity"
            }
            kingdomCheckDialog(
                game = this.game,
                kingdom = this.kingdom,
                kingdomActor = this.kingdomActor,
                check = CheckType.PerformActivity(activity),
                afterRoll = {
                    close()
                    if (it.succeeded()) {
                        buildUuid(item.uuid, item.name)
                    } else {
                        ""
                    }
                },
                overrideDc = getLevelBasedDC(item.level),
            )
        }
    }
}

fun armyTacticsBrowser(game: Game, kingdomActor: PF2ENpc, kingdom: KingdomData, army: PF2EArmy) {
    ArmyTacticsBrowser(game, kingdomActor, kingdom, army).launch()
}