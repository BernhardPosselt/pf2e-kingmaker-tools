package at.posselt.pfrpg2e.actions.handlers

import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.actions.ActionMessage
import at.posselt.pfrpg2e.camping.CampingActor
import at.posselt.pfrpg2e.camping.FoodAmount
import at.posselt.pfrpg2e.camping.HuntAndGatherData
import at.posselt.pfrpg2e.camping.addFoodToInventory
import at.posselt.pfrpg2e.camping.findHuntAndGatherTargetActor
import at.posselt.pfrpg2e.camping.getCamping
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import at.posselt.pfrpg2e.utils.postChatTemplate
import js.objects.recordOf

class AddHuntAndGatherResultHandler() : ActionHandler(
    action = "addHuntAndGatherResult",
) {
    override suspend fun execute(action: ActionMessage, dispatcher: ActionDispatcher) {
        val result = action.data.unsafeCast<HuntAndGatherData>()
        val campingActor = fromUuidTypeSafe<CampingActor>(result.campingActorUuid)
        campingActor?.getCamping()?.let { camping ->
            findHuntAndGatherTargetActor(result.actorUuid, camping, campingActor)
                ?.let {
                    it.addFoodToInventory(
                        FoodAmount(
                            basicIngredients = result.basicIngredients,
                            specialIngredients = result.specialIngredients,
                        )
                    )
                    postChatTemplate(
                        templatePath = "chatmessages/add-hunt-and-gather.hbs",
                        templateContext = recordOf(
                            "actorName" to it.name,
                            "basicIngredients" to result.basicIngredients,
                            "specialIngredients" to result.specialIngredients,
                        )
                    )
                }
        }
    }
}