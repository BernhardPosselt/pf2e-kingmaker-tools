package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel
import at.posselt.pfrpg2e.utils.asAnyObjectList
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.postChatTemplate
import com.foundryvtt.core.Actor
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Hooks
import com.foundryvtt.core.onPreUpdateActor
import com.foundryvtt.core.utils.getProperty
import com.foundryvtt.pf2e.actor.PF2ENpc
import js.objects.recordOf

private const val mealResultPath = "flags.${Config.moduleId}.camping-sheet.cooking.results"

private fun relevantUpdate(camping: CampingData, update: AnyObject): Boolean {
    val current = camping.cooking.results
        .sortedBy { it.recipeName }
    val updated = getProperty(update, mealResultPath)
        ?.unsafeCast<Array<CookingResult>>()
        ?.sortedBy { it.recipeName }
        ?: emptyList()
    return doObjectArraysDiffer(current.asAnyObjectList(), updated.asAnyObjectList())
}

private suspend fun checkPreActorMealUpdate(actor: Actor, update: AnyObject) {
    val camping = actor.takeIfInstance<PF2ENpc>()?.getCamping() ?: return
    console.log("Received camping update", update)
    if (!relevantUpdate(camping, update)) return
    val recipesByName = camping.getAllRecipes().associateBy { it.name }
    val current = camping.cooking.results.associateBy { it.recipeName }
    val updated = getProperty(update, mealResultPath)
        ?.unsafeCast<Array<CookingResult>>()
        ?: emptyArray()
    updated
        .filter {
            it.result != null && it.result != current[it.recipeName]?.result
        }
        .forEach { result ->
            val degree = result.result?.let { fromCamelCase<DegreeOfSuccess>(it) }
            val data = recipesByName[result.recipeName]
            val message = when (degree) {
                DegreeOfSuccess.CRITICAL_FAILURE -> data?.criticalFailure?.message
                DegreeOfSuccess.SUCCESS -> data?.success?.message
                DegreeOfSuccess.CRITICAL_SUCCESS -> data?.criticalSuccess?.message
                else -> null
            }
            postChatTemplate(
                templatePath = "chatmessages/apply-meal-result.hbs",
                templateContext = recordOf(
                    "name" to result.recipeName,
                    "degree" to result.result,
                    "label" to degree?.toLabel(),
                    "message" to message,
                    "failure" to (degree == DegreeOfSuccess.FAILURE),
                ),
            )
        }
}

fun registerMealDiffingHooks() {
    Hooks.onPreUpdateActor { actor, update, _, _ ->
        buildPromise {
            checkPreActorMealUpdate(actor, update)
        }
    }
}