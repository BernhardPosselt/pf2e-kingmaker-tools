package at.posselt.pfrpg2e.actions.handlers

import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.actions.ActionMessage
import at.posselt.pfrpg2e.camping.CampingActor
import at.posselt.pfrpg2e.camping.applyConsumptionMealEffects
import at.posselt.pfrpg2e.camping.cookingCost
import at.posselt.pfrpg2e.camping.discoverCost
import at.posselt.pfrpg2e.camping.getActorsCarryingFood
import at.posselt.pfrpg2e.camping.getAllRecipes
import at.posselt.pfrpg2e.camping.getCamping
import at.posselt.pfrpg2e.camping.getCampingActorByUuid
import at.posselt.pfrpg2e.camping.getCompendiumFoodItems
import at.posselt.pfrpg2e.camping.reduceFoodBy
import at.posselt.pfrpg2e.camping.setCamping
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import at.posselt.pfrpg2e.utils.postChatMessage
import com.foundryvtt.pf2e.actor.PF2ECharacter
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface LearnSpecialRecipeData {
    val campingActorUuid: String
    val actorUuid: String
    val id: String
    val degree: String
}

class LearnSpecialRecipeHandler() : ActionHandler("learnSpecialRecipe") {
    override suspend fun execute(action: ActionMessage, dispatcher: ActionDispatcher) {
        val data = action.data.unsafeCast<LearnSpecialRecipeData>()
        val campingActor = fromUuidTypeSafe<CampingActor>(data.campingActorUuid) ?: return
        val camping = campingActor.getCamping() ?: return
        val degreeOfSuccess = fromCamelCase<DegreeOfSuccess>(data.degree) ?: return
        val recipeId = data.id
        val actor = getCampingActorByUuid(data.actorUuid)
        val recipe = camping.getAllRecipes().find { it.id == recipeId }
        if (recipe != null && actor != null) {
            val cost = if (degreeOfSuccess == DegreeOfSuccess.CRITICAL_SUCCESS) {
                recipe.cookingCost()
            } else {
                recipe.discoverCost()
            }
            if (degreeOfSuccess == DegreeOfSuccess.CRITICAL_FAILURE && actor is PF2ECharacter) {
                actor.applyConsumptionMealEffects(recipe.criticalFailure)
            }
            reduceFoodBy(
                actors = camping.getActorsCarryingFood(campingActor),
                foodAmount = cost,
                foodItems = getCompendiumFoodItems(),
            )
            if (degreeOfSuccess.succeeded()) {
                camping.cooking.knownRecipes = (camping.cooking.knownRecipes + recipeId).distinct().toTypedArray()
                campingActor.setCamping(camping)
            }
            postChatMessage("Learned recipe ${recipe.name}")
        }
    }
}