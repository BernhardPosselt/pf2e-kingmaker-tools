package at.posselt.pfrpg2e.kingdom.armies

import at.posselt.pfrpg2e.kingdom.modifiers.penalties.ArmyConditionInfo
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.Actor
import com.foundryvtt.core.documents.Folder
import com.foundryvtt.core.utils.deepClone
import com.foundryvtt.core.utils.mergeObject
import com.foundryvtt.pf2e.actor.PF2EArmy
import js.array.toTypedArray
import js.objects.recordOf
import js.reflect.Reflect
import kotlinx.coroutines.await

fun Game.getSelectedArmyConditions() =
    getSelectedArmies()
        .firstOrNull()
        ?.let {
            val miredCount = it.miredValue() ?: 0
            val wearyCount = it.wearyValue() ?: 0
            ArmyConditionInfo(
                armyName = it.name,
                armyUuid = it.uuid,
                miredValue = miredCount,
                wearyValue = wearyCount,
                wearyLabel = t(
                    "modifiers.penalties.weary", recordOf(
                        "armyName" to it.name,
                        "count" to wearyCount,
                    )
                ),
                miredLabel = t(
                    "modifiers.penalties.mired", recordOf(
                        "armyName" to it.name,
                        "count" to miredCount,
                    )
                ),
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
            val merged = mergeObject(obj, update)
            Reflect.deleteProperty(merged, "_id")
            merged
        }
        ?.toTypedArray()
        ?: emptyArray()
    Actor.createDocuments(data).await()
    return folder
}

val PF2EArmy.isSpecial: Boolean
    get() = system.traits.rarity != "common"