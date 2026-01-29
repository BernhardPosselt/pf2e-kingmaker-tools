package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.utils.asAnyObjectList
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.postChatTemplate
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.documents.Actor
import com.foundryvtt.core.documents.onPreUpdateActor
import com.foundryvtt.core.helpers.TypedHooks
import com.foundryvtt.core.utils.diffObject
import com.foundryvtt.core.utils.getProperty
import js.objects.Object
import js.objects.recordOf

private const val mealResultPath = "flags.${Config.moduleId}.camping-sheet.cooking.results"

private fun doObjectArraysDiffer(source: List<AnyObject>, target: List<AnyObject>): Boolean {
    return source.size != target.size ||
            (source.asSequence() zip target.asSequence())
                .any { (first, second) ->
                    Object.keys(diffObject(first, second)).isNotEmpty()
                }
}

private fun relevantUpdate(camping: CampingData, update: AnyObject): Boolean {
    val current = camping.cooking.results
        .sortedBy { it.recipeId }
    val updated = getProperty(update, mealResultPath)
        ?.unsafeCast<Array<CookingResult>>()
        ?.sortedBy { it.recipeId }
        ?: emptyList()
    return doObjectArraysDiffer(current.asAnyObjectList(), updated.asAnyObjectList())
}

private suspend fun checkPreActorMealUpdate(actor: Actor, update: AnyObject) {
    val camping = actor.takeIfInstance<CampingActor>()?.getCamping() ?: return
    if (!relevantUpdate(camping, update)) return
    console.log("Received camping update", update)
    val recipesById = camping.getAllRecipes().associateBy { it.id }
    val current = camping.cooking.results.associateBy { it.recipeId }
    val updated = getProperty(update, mealResultPath)
        ?.unsafeCast<Array<CookingResult>>()
        ?: emptyArray()
    updated
        .filter {
            it.result != null && it.result != current[it.recipeId]?.result
        }
        .forEach { result ->
            val degree = result.result?.let { fromCamelCase<DegreeOfSuccess>(it) }
            val data = recipesById[result.recipeId]
            val message = when (degree) {
                DegreeOfSuccess.CRITICAL_FAILURE -> data?.criticalFailure?.message
                DegreeOfSuccess.SUCCESS -> data?.success?.message
                DegreeOfSuccess.CRITICAL_SUCCESS -> data?.criticalSuccess?.message
                else -> null
            }
            postChatTemplate(
                templatePath = "chatmessages/apply-meal-result.hbs",
                templateContext = recordOf(
                    "id" to result.recipeId,
                    "name" to data?.name,
                    "degree" to result.result,
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