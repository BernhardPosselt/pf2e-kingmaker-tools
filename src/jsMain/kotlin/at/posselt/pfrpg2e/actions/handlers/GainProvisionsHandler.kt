package at.posselt.pfrpg2e.actions.handlers

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.actions.ActionMessage
import at.posselt.pfrpg2e.camping.addConsumableToInventory
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import at.posselt.pfrpg2e.utils.postChatMessage
import com.foundryvtt.core.Actor
import com.foundryvtt.core.Game
import com.foundryvtt.core.fromUuid
import com.foundryvtt.pf2e.actor.PF2EActor
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface GainProvisions {
    val quantity: Int
    val actorUuid: String
}

class GainProvisionsHandler(
    private val game: Game,
) : ActionHandler("gainProvisions") {
    override suspend fun execute(action: ActionMessage, dispatcher: ActionDispatcher) {
        val data = action.data.unsafeCast<GainProvisions>()
        val quantity = data.quantity
        val actor = fromUuid(data.actorUuid).await()
            .unsafeCast<PF2EActor?>()
        if (quantity > 0 && actor != null) {
            actor.addConsumableToInventory(Config.items.provisionsUuid, quantity)
            postChatMessage("Adding $quantity provisions", speaker = actor)
        }
    }
}