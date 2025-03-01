package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.App
import at.posselt.pfrpg2e.app.HandlebarsFormApplicationOptions
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.kingdom.armies.getAllPlayerArmies
import at.posselt.pfrpg2e.kingdom.armies.importBasicArmies
import at.posselt.pfrpg2e.kingdom.armies.isSpecial
import at.posselt.pfrpg2e.toLabel
import at.posselt.pfrpg2e.utils.awaitAll
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.buildUuid
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.resolveTemplatePath
import com.foundryvtt.core.Game
import com.foundryvtt.core.applications.api.ApplicationPosition
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.applications.api.HandlebarsTemplatePart
import com.foundryvtt.core.applications.api.Window
import com.foundryvtt.core.ui
import com.foundryvtt.core.ui.TextEditor
import com.foundryvtt.pf2e.actor.PF2EArmy
import js.objects.JsPlainObject
import js.objects.recordOf
import kotlinx.coroutines.await
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

@JsPlainObject
private external interface ArmyContext {
    val link: String
    val type: String
    val dc: Int
    val special: Boolean
    val uuid: String
}


@JsPlainObject
private external interface ArmiesContext : HandlebarsRenderContext {
    val armies: Array<ArmyContext>
}

@JsExport
private class ArmyBrowser(
    private val game: Game,
) : App<ArmiesContext>(
    HandlebarsFormApplicationOptions(
        window = Window(
            title = "Recruitable, Player Owned Armies in 'Recruitable Armies' Folder",
        ),
        parts = recordOf(
            "form" to HandlebarsTemplatePart(
                template = resolveTemplatePath("applications/kingdom/army-browser.hbs"),
            )
        ),
        id = "kmArmys",
        position = ApplicationPosition(
            width = 600,
        )
    )
) {
    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "recruit-tactic" -> {
                buildPromise {
                    val uuid = target.dataset["uuid"] as String
                    recruit(uuid)
                }
            }
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<ArmiesContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val armies = game.getAllPlayerArmies()
            .asSequence()
            .sortedWith(compareBy<PF2EArmy> { it.system.details.level.value }.thenBy { it.name })
            .map {
                buildPromise { toView(it) }
            }
            .toList()
            .awaitAll()
            .toTypedArray()
        ArmiesContext(
            partId = parent.partId,
            armies = armies,
        )
    }

    private suspend fun toView(army: PF2EArmy) =
        ArmyContext(
            link = TextEditor.enrichHTML(buildUuid(army.uuid, army.name)).await(),
            type = army.system.traits.type.toLabel(),
            dc = army.system.recruitmentDC,
            special = army.isSpecial,
            uuid = army.uuid,
        )

    private suspend fun recruit(uuid: String) {
        val army = fromUuidTypeSafe<PF2EArmy>(uuid)
        if (army != null) {
            val link = buildUuid(army.uuid, army.name)
            // TODO
//            new CheckDialog(null, {
//                activity: 'recruit-army',
//                kingdom: this.kingdom,
//                overrideSkills: army.isSpecial ? {statecraft: 0} : {warfare: 0},
//                dc: actor.system.recruitmentDC,
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

suspend fun armyBrowser(game: Game) {
    val allPlayerArmies = game.getAllPlayerArmies()
    if (allPlayerArmies.isNotEmpty()) {
        ArmyBrowser(game).launch()
    } else if (allPlayerArmies.isEmpty() && game.user.isGM) {
        ui.notifications.info("Importing Basic Armies into 'Recruitable Armies' folder")
        game.importBasicArmies()
        ui.notifications.info("Import finished")
        ArmyBrowser(game).launch()
    } else {
        ui.notifications.error("No armies found in the 'Recruitable Armies' folder. Let your GM open this dialog to import basic armies")
    }
}