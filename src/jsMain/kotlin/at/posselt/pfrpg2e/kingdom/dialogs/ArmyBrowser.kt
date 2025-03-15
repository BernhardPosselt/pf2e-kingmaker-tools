package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.forms.SimpleApp
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRank
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.armies.getRecruitableArmies
import at.posselt.pfrpg2e.kingdom.armies.importBasicArmies
import at.posselt.pfrpg2e.kingdom.armies.isSpecial
import at.posselt.pfrpg2e.kingdom.getActivity
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.toLabel
import at.posselt.pfrpg2e.utils.awaitAll
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.buildUuid
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import at.posselt.pfrpg2e.utils.launch
import com.foundryvtt.core.Game
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.ui
import com.foundryvtt.core.ui.TextEditor
import com.foundryvtt.pf2e.actor.PF2EArmy
import js.objects.JsPlainObject
import kotlinx.coroutines.await
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

@JsPlainObject
external interface ArmyContext {
    val link: String
    val type: String
    val dc: Int
    val special: Boolean
    val uuid: String
}


@JsPlainObject
external interface ArmiesContext : HandlebarsRenderContext {
    val armies: Array<ArmyContext>
}

private class ArmyBrowser(
    private val game: Game,
    private val kingdomActor: KingdomActor,
    private val kingdom: KingdomData,
    private val folderName: String,
) : SimpleApp<ArmiesContext>(
    title = "Armies in 'Recruitable Armies' Folder",
    template = "applications/kingdom/army-browser.hbs",
    width = 600,
    id = "kmArmies-${kingdomActor.uuid}",
) {
    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "recruit-army" -> {
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
        val armies = game.getRecruitableArmies(folderName)
            .asSequence()
            .sortedWith(compareBy<PF2EArmy> { it.system.details.level.value }.thenBy { it.name })
            .map {
                buildPromise {
                    val link = TextEditor.enrichHTML(buildUuid(it.uuid, it.name)).await()
                    ArmyContext(
                        link = link,
                        type = it.system.traits.type.toLabel(),
                        dc = it.system.recruitmentDC,
                        special = it.isSpecial,
                        uuid = it.uuid,
                    )
                }
            }
            .toList()
            .awaitAll()
            .toTypedArray()
        ArmiesContext(
            partId = parent.partId,
            armies = armies,
        )
    }

    private suspend fun recruit(uuid: String) {
        val army = fromUuidTypeSafe<PF2EArmy>(uuid)
        if (army != null) {
            val activity = this.kingdom.getActivity("recruit-army")
            checkNotNull(activity) {
                "Could not find recruit-army activity"
            }
            val degreeMessage = "<b>Recruited Army</b>: ${buildUuid(army.uuid, army.name)}"
            kingdomCheckDialog(
                game = this.game,
                kingdom = this.kingdom,
                kingdomActor = this.kingdomActor,
                check = CheckType.PerformActivity(activity),
                afterRoll = { close() },
                overrideSkills = if (army.isSpecial) {
                    setOf(KingdomSkillRank(KingdomSkill.STATECRAFT, 0))
                } else {
                    setOf(KingdomSkillRank(KingdomSkill.WARFARE, 0))
                },
                degreeMessages = DegreeMessages(
                    criticalSuccess = degreeMessage,
                    success = degreeMessage,
                ),
                overrideDc = army.system.recruitmentDC,
            )
        }
    }
}

suspend fun armyBrowser(game: Game, kingdomActor: KingdomActor, kingdom: KingdomData) {
    val folderName = "Recruitable Armies"
    val allPlayerArmies = game.getRecruitableArmies(folderName)
    if (allPlayerArmies.isNotEmpty()) {
        ArmyBrowser(game, kingdomActor, kingdom, folderName).launch()
    } else if (allPlayerArmies.isEmpty() && game.user.isGM) {
        ui.notifications.info("Importing Basic Armies into '$folderName' folder")
        val folder = game.importBasicArmies(folderName)
        kingdom.settings.recruitableArmiesFolderId = folder.id
        kingdomActor.setKingdom(kingdom)
        ui.notifications.info("Import finished")
        ArmyBrowser(game, kingdomActor, kingdom, folderName).launch()
    } else {
        ui.notifications.error("No armies found in the '$folderName' folder. Let your GM open this dialog to import basic armies")
    }
}