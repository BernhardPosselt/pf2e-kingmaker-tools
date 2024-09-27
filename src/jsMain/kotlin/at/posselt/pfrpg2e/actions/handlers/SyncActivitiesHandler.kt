package at.posselt.pfrpg2e.actions.handlers

import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.actions.ActionMessage
import at.posselt.pfrpg2e.camping.CampingActivity
import at.posselt.pfrpg2e.camping.removeMealEffects
import at.posselt.pfrpg2e.camping.getActorsInCamp
import at.posselt.pfrpg2e.camping.getAllRecipes
import at.posselt.pfrpg2e.camping.getCamping
import at.posselt.pfrpg2e.camping.getCampingActor
import at.posselt.pfrpg2e.camping.syncCampingEffects
import at.posselt.pfrpg2e.camping.updateCampingPosition
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.utils.postChatTemplate
import com.foundryvtt.core.Game
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface SyncActivitiesAction {
    val activities: Array<CampingActivity>
    val rollRandomEncounter: Boolean
    val clearMealEffects: Boolean
    val prepareCampsiteResult: String?
}

class SyncActivitiesHandler(
    private val game: Game,
) : ActionHandler("syncActivities") {
    override suspend fun execute(action: ActionMessage, dispatcher: ActionDispatcher) {
        val data = action.data.unsafeCast<SyncActivitiesAction>()
        val campingActor = game.getCampingActor()
        val camping = campingActor?.getCamping()
        if (camping != null) {
            data.prepareCampsiteResult
                ?.let { fromCamelCase<DegreeOfSuccess>(it) }
                ?.let { result ->
                    if (result != DegreeOfSuccess.CRITICAL_FAILURE) {
                        camping.worldSceneId?.let {
                            updateCampingPosition(game, it, result)
                        }
                    }
                }
            if (data.clearMealEffects) {
                removeMealEffects(camping.getAllRecipes().toList(), camping.getActorsInCamp())
            }
            camping.syncCampingEffects(data.activities)
        }
        if (data.rollRandomEncounter) {
            postChatTemplate(
                "chatmessages/random-camping-encounter.hbs",
                rollMode = RollMode.BLINDROLL
            );
        }
    }
}
