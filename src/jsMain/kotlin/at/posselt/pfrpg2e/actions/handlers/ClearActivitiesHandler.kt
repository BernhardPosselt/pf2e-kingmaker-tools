package at.posselt.pfrpg2e.actions.handlers

import at.posselt.pfrpg2e.actions.ActionMessage
import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.camping.clearCampingEffects
import at.posselt.pfrpg2e.camping.getCamping
import at.posselt.pfrpg2e.camping.getCampingActor
import com.foundryvtt.core.Game

class ClearActivitiesHandler(
    private val game: Game,
) : ActionHandler("clearActivities") {
    override suspend fun execute(action: ActionMessage, dispatcher: ActionDispatcher) {
        game.getCampingActor()
            ?.getCamping()
            ?.clearCampingEffects()
    }
}