package at.posselt.pfrpg2e.kingdom.armies

import at.posselt.pfrpg2e.kingdom.modifiers.penalties.ArmyConditionInfo
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.Actor
import com.foundryvtt.core.documents.Folder
import com.foundryvtt.core.utils.deepClone
import com.foundryvtt.core.utils.mergeObject
import com.foundryvtt.pf2e.actor.PF2EArmy
import js.array.toTypedArray
import js.objects.recordOf
import kotlinx.coroutines.await

fun Game.getSelectedArmyConditions() =
    getSelectedArmies()
        .firstOrNull()
        ?.let {
            ArmyConditionInfo(
                armyName = it.name,
                armyUuid = it.uuid,
                miredValue = it.miredValue() ?: 0,
                wearyValue = it.wearyValue() ?: 0,
            )
        }

fun Game.getSelectedArmies(): Array<PF2EArmy> =
    canvas.tokens.controlled
        .asSequence()
        .mapNotNull { it.actor }
        .filterIsInstance<PF2EArmy>()
        .toTypedArray()

fun Game.getRecruitableArmies(folderName: String): Array<PF2EArmy> =
    actors.contents.asSequence()
        .filterIsInstance<PF2EArmy>()
        .filter { it.folder?.name == folderName }
        .toTypedArray()

suspend fun Game.importBasicArmies(folderName: String): Folder {
    val folder = Folder.create(
        recordOf(
            "name" to folderName,
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
    return folder
}

val PF2EArmy.isSpecial: Boolean
    get() = system.traits.rarity != "common"