package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.kingdom.KingdomActivity
import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actor.PF2ENpc
import js.objects.JsPlainObject


@JsPlainObject
external interface ModifierPill {
    val value: Int
    val label: String
}


suspend fun rollCheck(
    game: Game,
    afterRollMessage: AfterRollMessage,
    rollMode: RollMode?,
    activity: KingdomActivity?,
    modifier: Int,
    modifierPills: Array<ModifierPill>,
    dc: Int,
    kingdomActor: PF2ENpc,
) {

}