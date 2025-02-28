package at.posselt.pfrpg2e.kingdom.armies

import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actor.PF2EArmy
import js.array.toTypedArray
import js.iterable.asSequence

fun Game.getPlayerSelectedArmies(): Array<PF2EArmy> =
    user.targets.values()
        .asSequence()
        .filterIsInstance<PF2EArmy>()
        .toTypedArray()

fun Game.getAllPlayerArmies(): Array<PF2EArmy> =
    actors.contents.asSequence()
        .filterIsInstance<PF2EArmy>()
        .filter(PF2EArmy::hasPlayerOwner)
        .filter { it.folder?.name == "Recruitable Armies" }
        .toTypedArray()

fun highestScoutingDc(armies: List<PF2EArmy>): Int =
    armies.maxOfOrNull { it.system.scouting } ?: 0

