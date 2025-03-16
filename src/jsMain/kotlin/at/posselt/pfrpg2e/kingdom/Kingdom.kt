package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.utils.getAppFlag
import at.posselt.pfrpg2e.utils.setAppFlag
import com.foundryvtt.core.utils.deepClone
import com.foundryvtt.pf2e.actor.PF2EParty

typealias KingdomActor = PF2EParty

fun KingdomActor.getKingdom(): KingdomData? =
    getAppFlag<KingdomActor, KingdomData?>("kingdom-sheet")
        ?.let(::deepClone)

suspend fun KingdomActor.setKingdom(data: KingdomData) {
    setAppFlag("kingdom-sheet", data)
}

