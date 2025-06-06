package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.actions.ActionMessage
import at.posselt.pfrpg2e.actions.handlers.ApplyMealEffects
import at.posselt.pfrpg2e.actions.handlers.GainProvisions
import at.posselt.pfrpg2e.actions.handlers.LearnSpecialRecipeData
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.utils.bindChatClick
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import at.posselt.pfrpg2e.utils.postChatTemplate
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import js.objects.recordOf
import kotlinx.coroutines.await
import org.w3c.dom.get

suspend fun postPassTimeMessage(message: String, hours: Int) {
    postChatTemplate(
        templatePath = "chatmessages/pass-time.hbs",
        templateContext = recordOf(
            "message" to message,
            "seconds" to hours * 3600,
            "label" to t("camping.hours", recordOf("count" to hours))
        ),
        rollMode = RollMode.GMROLL,
    )
}

fun bindCampingChatEventListeners(game: Game, dispatcher: ActionDispatcher) {
    bindChatClick(".km-add-recipe") { _, el, _ ->
        val actorUuid = el.dataset["actorUuid"]
        val id = el.dataset["id"]
        val degree = el.dataset["degree"]
        val campingActorUuid = el.dataset["campingActorUuid"]
        if (actorUuid != null && degree != null && id != null && campingActorUuid != null) {
            buildPromise {
                dispatcher.dispatch(
                    ActionMessage(
                        action = "learnSpecialRecipe",
                        data = LearnSpecialRecipeData(
                            actorUuid = actorUuid,
                            id = id,
                            degree = degree,
                            campingActorUuid = campingActorUuid,
                        ).unsafeCast<AnyObject>()
                    )
                )
            }
        }
    }
    bindChatClick(".km-pass-time") { _, el, _ ->
        el.dataset["seconds"]?.toInt()?.let {
            buildPromise {
                game.time.advance(it).await()
            }
        }
    }
    bindChatClick(".km-random-encounter") { _, el, _ ->
        el.dataset["campingActorUuid"]?.let {
            buildPromise {
                fromUuidTypeSafe<CampingActor>(it)?.let { actor ->
                    buildPromise {
                        rollRandomEncounter(game, actor, true)
                    }
                }
            }
        }
    }
    bindChatClick(".gain-provisions") { _, el, _ ->
        buildPromise {
            val actorUuid = el.dataset["actorUuid"]
            val quantity = el.dataset["quantity"]?.toInt() ?: 0
            if (quantity > 0 && actorUuid != null) {
                dispatcher.dispatch(
                    ActionMessage(
                        action = "gainProvisions",
                        data = GainProvisions(
                            quantity = quantity,
                            actorUuid = actorUuid,
                        ).unsafeCast<AnyObject>()
                    )
                )
            }
        }
    }
    bindChatClick(".km-add-food") { _, el, _ ->
        el.dataset["campingActorUuid"]?.let {
            val action = ActionMessage(
                action = "addHuntAndGatherResult",
                data = HuntAndGatherData(
                    actorUuid = el.dataset["actorUuid"] as String,
                    basicIngredients = el.dataset["basicIngredients"]?.toInt() ?: 0,
                    specialIngredients = el.dataset["specialIngredients"]?.toInt() ?: 0,
                    campingActorUuid = it,
                ).unsafeCast<AnyObject>()
            )
            buildPromise {
                dispatcher.dispatch(action)
            }
        }
    }
    bindChatClick(".km-apply-meal-effect") { _, el, _ ->
        val degree = el.dataset["degree"]
        val id = el.dataset["recipe"]
        val campingActorUuid = el.dataset["campingActorUuid"]
        if (degree != null && id != null && campingActorUuid != null) {
            buildPromise {
                dispatcher.dispatch(
                    ActionMessage(
                        action = "applyMealEffects",
                        data = ApplyMealEffects(
                            degree = degree,
                            recipeId = id,
                            campingActorUuid = campingActorUuid,
                        ).unsafeCast<AnyObject>()
                    )
                )
            }
        }
    }
}