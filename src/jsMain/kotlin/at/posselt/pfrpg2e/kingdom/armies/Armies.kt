package at.posselt.pfrpg2e.kingdom.armies

import com.foundryvtt.core.Actor
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.Folder
import com.foundryvtt.core.utils.deepClone
import com.foundryvtt.core.utils.mergeObject
import com.foundryvtt.pf2e.actor.PF2EArmy
import js.array.toTypedArray
import js.iterable.asSequence
import js.objects.recordOf
import kotlinx.coroutines.await

fun Game.getTargetedArmies(): Array<PF2EArmy> =
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

suspend fun Game.importBasicArmies() {
    val folder = Folder.create(
        recordOf(
            "name" to "Recruitable Armies",
            "type" to "Actor",
            "parent" to null,
            "color" to null,
        )
    ).await()
    val data = packs.get("pf2e.kingmaker-bestiary")
        ?.getDocuments()
        ?.await()
        ?.asSequence()
        ?.filterIsInstance<PF2EArmy>()
        ?.filter { it.name.startsWith("Basic") }
        ?.map {
            val obj = deepClone(it.toObject())
            val update = recordOf(
                "folder" to folder.id,
                "permission" to 0,
                "ownership" to recordOf("default" to 3),
            )
            mergeObject(obj, update)
        }
        ?.toTypedArray()
        ?: emptyArray()
    Actor.createDocuments(data).await()
}

val PF2EArmy.isSpecial: Boolean
    get() = system.traits.rarity != "common"