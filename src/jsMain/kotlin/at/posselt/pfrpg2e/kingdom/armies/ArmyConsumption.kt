package at.posselt.pfrpg2e.kingdom.armies

import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.getKingdomActors
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.utils.buildPromise
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.Actor
import com.foundryvtt.core.documents.TokenDocument
import com.foundryvtt.core.documents.onCreateItem
import com.foundryvtt.core.documents.onCreateToken
import com.foundryvtt.core.documents.onDeleteActor
import com.foundryvtt.core.documents.onDeleteItem
import com.foundryvtt.core.documents.onDeleteScene
import com.foundryvtt.core.documents.onDeleteToken
import com.foundryvtt.core.documents.onUpdateActor
import com.foundryvtt.core.documents.onUpdateItem
import com.foundryvtt.core.documents.onUpdateToken
import com.foundryvtt.core.helpers.TypedHooks
import com.foundryvtt.pf2e.actor.PF2EArmy
import kotlin.math.max

private fun calculateTotalArmyConsumption(game: Game, folderId: String) =
    game.scenes.contents
        .asSequence()
        .flatMap { it.tokens.contents.toList() }
        .filterNot(TokenDocument::hidden)
        .filter { token ->
            val actor = token.actor
            if (actor is PF2EArmy && token.actorLink) {
                actor.folder?.id == folderId
            } else if (actor is PF2EArmy) {
                val baseActor = actor.parent?.takeIfInstance<TokenDocument>()?.baseActor
                baseActor is PF2EArmy && baseActor.folder?.id == folderId
            } else {
                false
            }
        }
        .map { it.actor }
        .filterIsInstance<PF2EArmy>()
        .distinctBy(PF2EArmy::uuid)
        .sumOf { it.system.consumption }

suspend fun updateArmyConsumption(game: Game) {
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
    TypedHooks.onCreateToken { document, _, _, _ -> document.actor?.let { buildPromise { checkedUpdate(game, it) } } }
    TypedHooks.onDeleteToken { document, _, _ -> document.actor?.let { buildPromise { checkedUpdate(game, it) } } }
    TypedHooks.onUpdateToken { document, _, _, _ -> document.actor?.let { buildPromise { checkedUpdate(game, it) } } }

    TypedHooks.onCreateItem { document, _, _, _ -> document.actor?.let { buildPromise { checkedUpdate(game, it) } } }
    TypedHooks.onDeleteItem { document, _, _ -> document.actor?.let { buildPromise { checkedUpdate(game, it) } } }
    TypedHooks.onUpdateItem { document, _, _, _ -> document.actor?.let { buildPromise { checkedUpdate(game, it) } } }

    TypedHooks.onDeleteActor { document, _, _ -> buildPromise { checkedUpdate(game, document) } }
    TypedHooks.onUpdateActor { document, _, _, _ -> buildPromise { checkedUpdate(game, document) } }

    TypedHooks.onDeleteScene { document, _, _ -> buildPromise { updateArmyConsumption(game) } }
}