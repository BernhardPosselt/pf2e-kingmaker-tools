package at.posselt.pfrpg2e.kingdom.armies

import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.getKingdomActors
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

private fun calculateTotalArmyConsumption(game: Game, folderId: String) =
    game.scenes.contents
        .asSequence()
        .filter { it.hasPlayerOwner }
        .flatMap { it.tokens.contents.toList() }
        .filterNot(TokenDocument::hidden)
        .mapNotNull(TokenDocument::actor)
        .filterIsInstance<PF2EArmy>()
        .filter {
            val baseActor = it.baseActor
            baseActor is PF2EArmy && baseActor.folder?.id == folderId
        }
        .distinctBy(PF2EArmy::uuid)
        .sumOf { it.system.consumption }

private suspend fun updateArmyConsumption(game: Game) {
    game.getKingdomActors()
        .forEach {
            val kingdom = it.getKingdom()
            val folderId = kingdom?.settings?.recruitableArmiesFolderId
            if (kingdom != null && kingdom.settings.autoCalculateArmyConsumption && folderId != null) {
                kingdom.consumption.armies = max(calculateTotalArmyConsumption(game, folderId), 0)
                it.setKingdom(kingdom)
            }
        }
}

private suspend fun checkedUpdate(game: Game, hookActor: Actor) {
    if (hookActor is PF2EArmy) {
        updateArmyConsumption(game)
    }
}

fun registerArmyConsumptionHooks(game: Game) {
    Hooks.onCreateToken { document, _, _, _ -> document.actor?.let { buildPromise { checkedUpdate(game, it) } } }
    Hooks.onDeleteToken { document, _, _ -> document.actor?.let { buildPromise { checkedUpdate(game, it) } } }
    Hooks.onUpdateToken { document, _, _, _ -> document.actor?.let { buildPromise { checkedUpdate(game, it) } } }

    Hooks.onCreateItem { document, _, _, _ -> document.actor?.let { buildPromise { checkedUpdate(game, it) } } }
    Hooks.onDeleteItem { document, _, _ -> document.actor?.let { buildPromise { checkedUpdate(game, it) } } }
    Hooks.onUpdateItem { document, _, _, _ -> document.actor?.let { buildPromise { checkedUpdate(game, it) } } }

    Hooks.onDeleteActor { document, _, _ -> buildPromise { checkedUpdate(game, document) } }
    Hooks.onUpdateActor { document, _, _, _ -> buildPromise { checkedUpdate(game, document) } }

    Hooks.onDeleteScene { document, _, _ -> buildPromise { updateArmyConsumption(game) } }
}