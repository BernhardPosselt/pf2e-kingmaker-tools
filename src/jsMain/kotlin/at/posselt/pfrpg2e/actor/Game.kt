package at.posselt.pfrpg2e.actor

import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actor.PF2ECharacter
import com.foundryvtt.pf2e.actor.PF2ENpc
import com.foundryvtt.pf2e.actor.PF2EParty
import js.array.toTypedArray

val Game.isKingmakerInstalled: Boolean
    get() = modules.get("pf2e-kingmaker")?.active ?: false


fun PF2EParty.partyMembers(): Array<PF2ECharacter> =
    members
        .filterIsInstance<PF2ECharacter>()
        .toTypedArray()

fun Game.npcs(): Array<PF2ENpc> =
    actors.contents
        .asSequence()
        .filterIsInstance<PF2ENpc>()
        .toTypedArray()
