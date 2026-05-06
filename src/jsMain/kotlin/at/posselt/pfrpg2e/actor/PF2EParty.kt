package at.posselt.pfrpg2e.actor

import at.posselt.pfrpg2e.utils.asSequence
import at.posselt.pfrpg2e.utils.toRecord
import com.foundryvtt.pf2e.actor.PF2EParty
import js.array.component1
import js.array.component2

fun PF2EParty.ownershipOwnersOnly() =
    members.asSequence()
        .filter { it.hasPlayerOwner }
        .flatMap { actor ->
            actor.ownership.asSequence()
                .filter { it.component2() == 3 }
        }
        .distinctBy { it.component1() }
        .toRecord()