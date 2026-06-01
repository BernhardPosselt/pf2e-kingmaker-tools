package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.utils.getAppFlag
import at.posselt.pfrpg2e.utils.setAppFlag
import at.posselt.pfrpg2e.utils.unsetAppFlag
import com.foundryvtt.core.documents.Actor
import com.foundryvtt.core.utils.deepClone
import com.foundryvtt.pf2e.actor.PF2EParty

import com.foundryvtt.core.game

typealias KingdomActor = PF2EParty

fun KingdomActor.getKingdom(): KingdomData? =
    getAppFlag<KingdomActor, KingdomData?>("kingdom-sheet")
        ?.let(::deepClone)

suspend fun KingdomActor.setKingdom(data: KingdomData) {
    setAppFlag("kingdom-sheet", data)
    data.companions?.forEach { companion ->
        val uuid = companion.actorUuid
        if (uuid != null) {
            val companionActor = game.actors.get(uuid)
            if (companionActor != null) {
                companionActor.setAppFlag("companion-data", companion)
            }
        }
    }
}

suspend fun KingdomActor.clearKingdom() {
    unsetAppFlag("kingdom-sheet")
}

fun Actor.isKingdomActor() =
    this is KingdomActor && getKingdom() != null