package at.posselt.pfrpg2e.actions.handlers

import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.actions.ActionMessage
import at.posselt.pfrpg2e.camping.MealChoice
import at.posselt.pfrpg2e.camping.applyConsumptionMealEffects
import at.posselt.pfrpg2e.camping.findCookingChoices
import at.posselt.pfrpg2e.camping.getActorsCarryingFood
import at.posselt.pfrpg2e.camping.getActorsInCamp
import at.posselt.pfrpg2e.camping.getAllRecipes
import at.posselt.pfrpg2e.camping.getCamping
import at.posselt.pfrpg2e.camping.getCampingActor
import at.posselt.pfrpg2e.camping.getCompendiumFoodItems
import at.posselt.pfrpg2e.camping.reduceFoodBy
import at.posselt.pfrpg2e.camping.sum
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.fromCamelCase
import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actor.PF2ECharacter
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ApplyMealEffects {
    val recipe: String
    val degree: String
}

class ApplyMealEffectsHandler(val game: Game) : ActionHandler("applyMealEffects") {
    override suspend fun execute(action: ActionMessage, dispatcher: ActionDispatcher) {
        val camping = game.getCampingActor()?.getCamping() ?: return
        val recipesByName = camping.getAllRecipes().associateBy { it.name }
        val data = action.data.unsafeCast<ApplyMealEffects>()
        val degree = fromCamelCase<DegreeOfSuccess>(data.degree) ?: return
        val recipe = recipesByName[data.recipe] ?: return

        // reduce meal cost
        val charactersInCampByUuid = camping.getActorsInCamp().filterIsInstance<PF2ECharacter>().associateBy { it.uuid }
        val parsed = camping.findCookingChoices(
            charactersInCampByUuid = charactersInCampByUuid,
            recipesByName = recipesByName
        )
        val mealChoices = parsed.meals
            .filterIsInstance<MealChoice.ParsedMeal>()
        val chosenMeals = mealChoices
            .filter { it.name == data.recipe }
        val totalCost = chosenMeals.map { it.cookingCost }.sum()
        reduceFoodBy(
            actors = camping.getActorsCarryingFood(game),
            foodItems = getCompendiumFoodItems(),
            foodAmount = totalCost,
        )

        // and apply effects if not failure
        val outcome = when (degree) {
            DegreeOfSuccess.CRITICAL_FAILURE -> recipe.criticalFailure
            DegreeOfSuccess.SUCCESS -> recipe.success
            DegreeOfSuccess.CRITICAL_SUCCESS -> recipe.criticalSuccess
            else -> null
        } ?: return
        val actors = chosenMeals.map { it.actor }
        val actorUuids = actors.map { it.uuid }.toSet()

        applyConsumptionMealEffects(
            actors = actors,
            outcome = outcome,
        )

        recipe.favoriteMeal?.let { favoriteOutcome ->
            val favoriteMealActors = mealChoices
                .filter { it.favoriteMeal?.name == data.recipe && it.actor.uuid in actorUuids }
                .map { it.actor }
            applyConsumptionMealEffects(
                actors = favoriteMealActors,
                outcome = favoriteOutcome,
            )
        }
    }
}