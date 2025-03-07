package at.posselt.pfrpg2e.actions.handlers

import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.actions.ActionMessage
import at.posselt.pfrpg2e.kingdom.sheet.openKingdomSheet
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actor.PF2ENpc
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface OpenKingdomSheetAction {
    val actorUuid: String
}

class OpenKingdomSheetHandler(
    private val game: Game,
) : ActionHandler(
    action = "openKingdomSheet",
    mode = ExecutionMode.OTHERS,
) {
    override suspend fun execute(action: ActionMessage, dispatcher: ActionDispatcher) {
        val data = action.data.unsafeCast<OpenKingdomSheetAction>()
        val actor = fromUuidTypeSafe<PF2ENpc>(data.actorUuid)
        openKingdomSheet(game, dispatcher, actor)
    }
}