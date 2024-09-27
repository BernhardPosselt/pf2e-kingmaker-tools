package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.actions.ActionMessage
import at.posselt.pfrpg2e.actions.handlers.GainProvisions
import at.posselt.pfrpg2e.actor.party
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.app.prompt
import at.posselt.pfrpg2e.camping.addConsumableToInventory
import at.posselt.pfrpg2e.utils.postChatMessage
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import js.objects.recordOf
import kotlinx.browser.localStorage
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface CreateFoodData {
    val amount: Int
}

suspend fun createFoodMacro(game: Game, dispatcher: ActionDispatcher) {
    prompt<CreateFoodData, Unit>(
        title = "How Many Provisions?",
        templatePath = "components/forms/form.hbs",
        templateContext = recordOf(
            "formRows" to formContext(
                NumberInput(
                    name = "amount",
                    label = "Amount",
                    value = localStorage.getItem("kmCreateFood")?.toInt() ?: 0
                )
            )
        )
    ) {
        val quantity = it.amount
        val actorUuid = game.party()?.uuid
        if (quantity > 0 && actorUuid != null) {
            localStorage.setItem("kmCreateFood", quantity.toString())
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