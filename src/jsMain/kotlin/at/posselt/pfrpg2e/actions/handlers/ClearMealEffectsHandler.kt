package at.posselt.pfrpg2e.actions.handlers

import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.actions.ActionMessage
import at.posselt.pfrpg2e.camping.removeMealEffects
import at.posselt.pfrpg2e.camping.getActorsInCamp
import at.posselt.pfrpg2e.camping.getAllRecipes
import at.posselt.pfrpg2e.camping.getCamping
import at.posselt.pfrpg2e.camping.getCampingActor
import com.foundryvtt.core.Game

class ClearMealEffectsHandler(
    private val game: Game,
) : ActionHandler("clearMealEffects") {
    override suspend fun execute(action: ActionMessage, dispatcher: ActionDispatcher) {
        game.getCampingActor()
            ?.getCamping()
            ?.let {
                removeMealEffects(it.getAllRecipes().toList(), it.getActorsInCamp())
            }
    }
}