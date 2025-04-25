package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.kingdom.structures.isStructure
import at.posselt.pfrpg2e.takeIfInstance
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.Actor
import com.foundryvtt.core.documents.TokenDocument
import com.foundryvtt.core.game
import com.foundryvtt.pf2e.actor.PF2ENpc
import js.objects.recordOf
import kotlinx.coroutines.await

private suspend fun TokenDocument.showHpBars() {
    update(recordOf("displayBars" to 50)).await()
}

private inline fun <reified T : Actor> TokenDocument.getActor(): T? {
    val tokenActor = actor
    return if (tokenActor is T && actorLink) {
        tokenActor
    } else if (tokenActor is T) {
        tokenActor.parent
            ?.takeIfInstance<TokenDocument>()
            ?.baseActor
            ?.takeIfInstance<T>()
    } else {
        null
    }
}

suspend fun Game.showAllNpcHpBars() {
    game.scenes.contents
        .flatMap { it.tokens.contents.toList() }
        .filter { it.getActor<PF2ENpc>() != null && !it.isStructure() }
        .forEach { token -> token.showHpBars() }
}