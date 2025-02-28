package at.posselt.pfrpg2e.kingdom.armies

import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.getKingdomActor
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.utils.buildPromise
import com.foundryvtt.core.Actor
import com.foundryvtt.core.Game
import com.foundryvtt.core.Hooks
import com.foundryvtt.core.documents.TokenDocument
import com.foundryvtt.core.documents.onCreateItem
import com.foundryvtt.core.documents.onCreateToken
import com.foundryvtt.core.documents.onDeleteItem
import com.foundryvtt.core.documents.onDeleteScene
import com.foundryvtt.core.documents.onDeleteToken
import com.foundryvtt.core.documents.onUpdateItem
import com.foundryvtt.core.documents.onUpdateToken
import com.foundryvtt.core.onDeleteActor
import com.foundryvtt.core.onUpdateActor
import com.foundryvtt.pf2e.actor.PF2EArmy
import kotlin.math.max

private fun calculateTotalArmyConsumption(game: Game) =
    game.scenes.contents
        .asSequence()
        .flatMap { it.tokens.contents.toList() }
        .filterNot(TokenDocument::hidden)
        .map(TokenDocument::actor)
        .filterIsInstance<PF2EArmy>()
        .filter(PF2EArmy::hasPlayerOwner)
        .distinctBy(PF2EArmy::uuid)
        .sumOf { it.system.consumption }

private suspend fun updateArmyConsumption(game: Game) {
    val kingdomActor = game.getKingdomActor()
    val kingdom = kingdomActor?.getKingdom()
    if (kingdom != null && kingdom.settings.autoCalculateArmyConsumption) {
        kingdom.consumption.armies = max(calculateTotalArmyConsumption(game), 0)
        kingdomActor.setKingdom(kingdom)
    }
}

private suspend fun checkedUpdate(game: Game, hookActor: Actor) {
    if (hookActor is PF2EArmy) {
        updateArmyConsumption(game)
    }
}

fun registerArmyConsumptionHooks(game: Game) {
    Hooks.onCreateToken { document, _, _, _ -> document.actor?.let { buildPromise {  checkedUpdate(game, it) } } }
    Hooks.onDeleteToken { document, _, _ -> document.actor?.let { buildPromise {  checkedUpdate(game, it) } } }
    Hooks.onUpdateToken { document, _, _, _ -> document.actor?.let { buildPromise {  checkedUpdate(game, it) } } }

    Hooks.onCreateItem { document, _, _, _ -> document.actor?.let { buildPromise {  checkedUpdate(game, it) } } }
    Hooks.onDeleteItem { document, _, _ -> document.actor?.let { buildPromise {  checkedUpdate(game, it) } } }
    Hooks.onUpdateItem { document, _, _, _ -> document.actor?.let { buildPromise {  checkedUpdate(game, it) } } }

    Hooks.onDeleteActor { document, _, _ -> buildPromise {  checkedUpdate(game, document) } }
    Hooks.onUpdateActor { document, _, _, _ -> buildPromise {  checkedUpdate(game, document) } }

    Hooks.onDeleteScene { document, _, _ -> buildPromise { updateArmyConsumption(game) }}
}