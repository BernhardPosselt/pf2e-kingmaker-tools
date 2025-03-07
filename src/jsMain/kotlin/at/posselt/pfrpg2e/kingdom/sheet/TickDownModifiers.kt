package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.setKingdom
import com.foundryvtt.pf2e.actor.PF2ENpc

suspend fun tickDownModifiers(kingdomActor: PF2ENpc, kingdom: KingdomData) {
    kingdom.modifiers = kingdom.modifiers.mapNotNull {
        val turns = it.turns
        if (turns == 0 || turns == null) {
            it
        } else if (turns == 1) {
            null
        } else {
            it.copy(turns = turns - 1)
        }
    }.toTypedArray()
    kingdomActor.setKingdom(kingdom)
}