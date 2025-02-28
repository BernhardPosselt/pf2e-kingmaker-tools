package at.posselt.pfrpg2e.kingdom.armies

import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.getKingdomActor
import com.foundryvtt.core.Actor
import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actor.PF2EArmy

private fun calculateTotalArmyConsumption(game: Game): Int {
//    game.scenes.contents.flatMap { it.tokens }
    return 0
}

fun updateArmyConsumption(
    game: Game,
    actor: Actor,
) {
    val kingdomActor = game.getKingdomActor()
    val kingdom = kingdomActor?.getKingdom()
    if (actor is PF2EArmy && kingdom != null) {

    }
}