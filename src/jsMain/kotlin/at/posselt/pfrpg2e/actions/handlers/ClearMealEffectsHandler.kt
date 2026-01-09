package at.posselt.pfrpg2e.actions.handlers

import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.actions.ActionMessage
import at.posselt.pfrpg2e.camping.CampingActor
import at.posselt.pfrpg2e.camping.getActorsInCamp
import at.posselt.pfrpg2e.camping.getAllRecipes
import at.posselt.pfrpg2e.camping.getCamping
import at.posselt.pfrpg2e.camping.removeMealEffects
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ClearMealEffectsMessage {
    val campingActorUuid: String
}

class ClearMealEffectsHandler : ActionHandler("clearMealEffects") {
    override suspend fun execute(action: ActionMessage, dispatcher: ActionDispatcher) {
        val data = action.data.unsafeCast<ClearMealEffectsMessage>()
        val campingActor = fromUuidTypeSafe<CampingActor>(data.campingActorUuid)
        campingActor
            ?.getCamping()
            ?.let {
                removeMealEffects(
                    recipes = it.getAllRecipes().toList(),
                    actors = it.getActorsInCamp(),
                    onlyRemoveAfterRest = false,
                    removeWhenPreparingCampsite = true
                )
            }
    }
}