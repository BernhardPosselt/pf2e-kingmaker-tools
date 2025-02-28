package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.App
import at.posselt.pfrpg2e.app.HandlebarsFormApplicationOptions
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.data.checks.getLevelBasedDC
import at.posselt.pfrpg2e.kingdom.armies.getAllAvailableArmyTactics
import at.posselt.pfrpg2e.kingdom.armies.hasTactic
import at.posselt.pfrpg2e.kingdom.armies.isArmyTactic
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.utils.awaitAll
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.buildUuid
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.resolveTemplatePath
import com.foundryvtt.core.Game
import com.foundryvtt.core.applications.api.ApplicationPosition
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.applications.api.HandlebarsTemplatePart
import com.foundryvtt.core.applications.api.Window
import com.foundryvtt.core.fromUuid
import com.foundryvtt.core.ui.TextEditor
import com.foundryvtt.pf2e.actor.PF2EArmy
import com.foundryvtt.pf2e.item.PF2ECampaignFeature
import com.foundryvtt.pf2e.item.itemFromUuid
import js.objects.JsPlainObject
import js.objects.recordOf
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

@JsExport
private class ArmyTacticsBrowser(
    private val game: Game,
    private val army: PF2EArmy,
) : App<ArmyTacticsContext>(
    HandlebarsFormApplicationOptions(
        window = Window(
            title = "Kingdom Settings",
        ),
        parts = recordOf(
            "form" to HandlebarsTemplatePart(
                template = resolveTemplatePath("applications/kingdom/army-tactics-browser.hbs"),
            )
        ),
        id = "kmArmyTactics",
        position = ApplicationPosition(
            width = 600,
        )
    )
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
            .sortedWith(compareBy<PF2ECampaignFeature> { it.system.level.value }.thenBy { it.name })
            .map {
                buildPromise { toView(it) }
            }
            .toList()
            .awaitAll()
            .toTypedArray()
        ArmyTacticsContext(
            partId = parent.partId,
            tactics = tactics
        )
    }

    private suspend fun toView(feature: PF2ECampaignFeature): ArmyTacticContext {
        val level = feature.system.level.value
        val dc = getLevelBasedDC(level)
        return ArmyTacticContext(
            link = TextEditor.enrichHTML(buildUuid(feature.uuid, feature.name)).await(),
            level = level,
            dc = dc,
            enabled = level <= army.level,
            uuid = feature.uuid,
        )
    }

    private suspend fun trainTactic(uuid: String) {
        val item = itemFromUuid(uuid)
            ?.takeIfInstance<PF2ECampaignFeature>()
            ?.takeIf { it.isArmyTactic }
        if (item != null) {
            val link = buildUuid(item.uuid, item.name)
            // TODO
//            new CheckDialog(null, {
//                activity: 'train-army',
//                kingdom: this.kingdom,
//                dc: getLevelBasedDC(item.system.level.value),
//                game: this.game,
//                type: 'activity',
//                onRoll: this.onRoll,
//                actor: this.sheetActor,
//                afterRoll: async (): Promise<void> => {
//                await this.close();
//            },
//                additionalChatMessages: [{
//                [DegreeOfSuccess.CRITICAL_SUCCESS]: link,
//                [DegreeOfSuccess.SUCCESS]: link,
//            }],
//            }).render(true);
        }
    }
}

fun armyTacticsBrowser(game: Game, army: PF2EArmy) {
    ArmyTacticsBrowser(game, army).launch()
}