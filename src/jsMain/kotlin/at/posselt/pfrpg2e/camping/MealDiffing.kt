package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.utils.asAnyObject
import at.posselt.pfrpg2e.utils.asSequence
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.parseChanges
import at.posselt.pfrpg2e.utils.postChatTemplate
import at.posselt.pfrpg2e.utils.t
import at.posselt.pfrpg2e.utils.toMap
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.documents.Actor
import com.foundryvtt.core.documents.onPreUpdateActor
import com.foundryvtt.core.helpers.TypedHooks
import com.foundryvtt.core.utils.getProperty
import js.array.component1
import js.array.component2
import js.objects.Record
import js.objects.recordOf

private suspend fun checkPreActorMealUpdate(actor: Actor, update: AnyObject) {
    val camping = actor.takeIfInstance<CampingActor>()?.getCamping() ?: return
    val campingUpdate = getProperty(update, campingPath).unsafeCast<AnyObject?>() ?: return
    val changes = parseChanges(camping.asAnyObject(), campingUpdate, setOf("results"), "cooking") ?: return
    val applied = getProperty(changes.applied, "cooking.results").unsafeCast<Record<String, CookingResult>?>() ?: return
    console.log("Received camping update", update, applied)
    val recipesById = camping.getAllRecipes().associateBy { it.id }
    val current = camping.cooking.results.toMap()
    applied.asSequence()
        .filter { (recipeId, data) -> data.result != null && data.result != current[recipeId]?.result }
        .forEach { (recipeId, data) ->
            val degree = data.result?.let { fromCamelCase<DegreeOfSuccess>(it) }
            val recipeData = recipesById[recipeId]
            val message = when (degree) {
                DegreeOfSuccess.CRITICAL_FAILURE -> recipeData?.criticalFailure?.message
                DegreeOfSuccess.SUCCESS -> recipeData?.success?.message
                DegreeOfSuccess.CRITICAL_SUCCESS -> recipeData?.criticalSuccess?.message
                else -> null
            }
            postChatTemplate(
                templatePath = "chatmessages/apply-meal-result.hbs",
                templateContext = recordOf(
                    "id" to recipeId,
                    "name" to recipeData?.name,
                    "degree" to data.result,
                    "label" to degree?.let { t(it) },
                    "message" to message,
                    "failure" to (degree == DegreeOfSuccess.FAILURE),
                    "campingActorUuid" to actor.uuid,
                ),
            )
        }
}

fun registerMealDiffingHooks() {
    TypedHooks.onPreUpdateActor { actor, update, _, _ ->
        buildPromise {
            checkPreActorMealUpdate(actor, update)
        }
    }
}