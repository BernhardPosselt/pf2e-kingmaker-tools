package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.postChatTemplate
import com.foundryvtt.core.AnyObject
import kotlinx.js.JsPlainObject

@JsPlainObject
private external interface DiscoverSpecialMealChatContext {
    val degree: String
    val name: String
    val actorUuid: String
    val learnRecipe: Boolean
    val criticalFailure: Boolean
}

suspend fun postDiscoverSpecialMeal(
    actorUuid: String,
    recipe: RecipeData,
    degreeOfSuccess: DegreeOfSuccess
) {
    postChatTemplate(
        templatePath = "chatmessages/discover-special-meal.hbs",
        templateContext = DiscoverSpecialMealChatContext(
            name = recipe.name,
            actorUuid = actorUuid,
            degree = degreeOfSuccess.toCamelCase(),
            learnRecipe = degreeOfSuccess.succeeded(),
            criticalFailure = degreeOfSuccess == DegreeOfSuccess.CRITICAL_FAILURE,
        ).unsafeCast<AnyObject>(),
    )
}
