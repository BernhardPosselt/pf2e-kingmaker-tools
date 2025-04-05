package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.postChatTemplate
import com.foundryvtt.core.AnyObject
import kotlinx.js.JsPlainObject

@Suppress("unused")
@JsPlainObject
private external interface DiscoverSpecialMealChatContext {
    val id: String
    val degree: String
    val name: String
    val actorUuid: String
    val learnRecipe: Boolean
    val criticalFailure: Boolean
    val campingActorUuid: String
}

suspend fun postDiscoverSpecialMeal(
    actorUuid: String,
    recipe: RecipeData,
    degreeOfSuccess: DegreeOfSuccess,
    campingActorUuid: String,
) {
    postChatTemplate(
        templatePath = "chatmessages/discover-special-meal.hbs",
        templateContext = DiscoverSpecialMealChatContext(
            id = recipe.id,
            name = recipe.name,
            actorUuid = actorUuid,
            degree = degreeOfSuccess.toCamelCase(),
            learnRecipe = degreeOfSuccess.succeeded(),
            criticalFailure = degreeOfSuccess == DegreeOfSuccess.CRITICAL_FAILURE,
            campingActorUuid = campingActorUuid,
        ).unsafeCast<AnyObject>(),
    )
}
