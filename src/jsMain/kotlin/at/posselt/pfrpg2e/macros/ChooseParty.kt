package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.Game
import com.foundryvtt.core.ui
import com.foundryvtt.pf2e.actor.PF2EParty

fun chooseParty(game: Game): PF2EParty {
    val parties = game.actors.contents.filterIsInstance<PF2EParty>()
    val first = parties.firstOrNull()
    if (first == null) {
        val message = t("macros.noPartiesFound")
        ui.notifications.error(message)
        throw IllegalStateException(message)
    }
    return if (parties.size > 1) {
        parties.find { it.active }!!
    } else {
        first
    }
}