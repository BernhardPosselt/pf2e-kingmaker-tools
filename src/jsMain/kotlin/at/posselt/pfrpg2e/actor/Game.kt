package at.posselt.pfrpg2e.actor

import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actor.*
import js.array.toTypedArray

val Game.isKingmakerInstalled: Boolean
    get() = modules.get("pf2e-kingmaker")?.active ?: false

fun Game.averagePartyLevel(): Int =
    partyMembers()
        .map { it.level }
        .average()
        .toInt()

fun Game.party(): PF2EParty? =
    actors.contents
        .asSequence()
        .filterIsInstance<PF2EParty>()
        .firstOrNull()


fun Game.partyMembers(): Array<PF2ECharacter> =
    actors.contents
        .asSequence()
        .filterIsInstance<PF2EParty>()
        .flatMap { it.members.asSequence() }
        .filterIsInstance<PF2ECharacter>()
        .toTypedArray()

fun Game.playerCharacters(): Array<PF2ECharacter> =
    actors.contents
        .asSequence()
        .filterIsInstance<PF2ECharacter>()
        .filter { it.hasPlayerOwner }
        .toTypedArray()

fun Game.npcs(): Array<PF2ENpc> =
    actors.contents
        .asSequence()
        .filterIsInstance<PF2ENpc>()
        .toTypedArray()

fun Game.playerFamiliars(): Array<PF2EFamiliar> =
    actors.contents
        .asSequence()
        .filterIsInstance<PF2EFamiliar>()
        .filter { it.hasPlayerOwner }
        .toTypedArray()

fun Game.playerArmies(): Array<PF2EArmy> =
    actors.contents
        .asSequence()
        .filterIsInstance<PF2EArmy>()
        .filter { it.hasPlayerOwner }
        .toTypedArray()