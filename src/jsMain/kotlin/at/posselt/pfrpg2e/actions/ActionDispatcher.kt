package at.posselt.pfrpg2e.actions

import at.posselt.pfrpg2e.actions.handlers.ActionHandler
import at.posselt.pfrpg2e.actions.handlers.ExecutionMode
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.emitPfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.utils.isFirstGM
import at.posselt.pfrpg2e.utils.isJsObject
import at.posselt.pfrpg2e.utils.onPfrpg2eKingdomCampingWeather
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game

class ActionDispatcher(
    val game: Game,
    val handlers: List<ActionHandler>,
    val debug: Boolean = true,
) {
    fun listen() {
        game.socket.onPfrpg2eKingdomCampingWeather { message ->
            if (debug) console.log("Received Socket Message", message)
            if (isJsObject(message)) {
                val action = message["action"]
                val data = message["data"]
                if (action is String && isJsObject(data)) {
                    buildPromise {
                        dispatch(message.unsafeCast<ActionMessage>(), true)
                    }
                }
            }
        }
    }

    suspend fun dispatch(action: ActionMessage, receivedViaSocket: Boolean = false) {
        if (debug) console.log("Dispatching action", action)
        val handler = handlers.find { it.canExecute(action) }
        if (handler != null) {
            if (handler.mode == ExecutionMode.GM_ONLY && game.isFirstGM()) {
                handler.execute(action, this)
            } else if (handler.mode == ExecutionMode.GM_ONLY && !game.isFirstGM() && !receivedViaSocket) {
                game.socket.emitPfrpg2eKingdomCampingWeather(action.unsafeCast<AnyObject>())
            } else if (handler.mode == ExecutionMode.OTHERS) {
                // break endless socket emitting circuit
                if (!receivedViaSocket) {
                    game.socket.emitPfrpg2eKingdomCampingWeather(action.unsafeCast<AnyObject>())
                } else {
                    handler.execute(action, this)
                }
            } else if (handler.mode == ExecutionMode.ALL) {
                handler.execute(action, this)
                // break endless socket emitting circuit
                if (!receivedViaSocket) {
                    game.socket.emitPfrpg2eKingdomCampingWeather(action.unsafeCast<AnyObject>())
                }
            }
        } else {
            if (debug) console.log("No handler found for action", action)
        }
    }
}