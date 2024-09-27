package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.utils.getAppFlag
import at.posselt.pfrpg2e.utils.setAppFlag
import at.posselt.pfrpg2e.utils.unsetAppFlag
import com.foundryvtt.core.utils.deepClone
import com.foundryvtt.pf2e.actor.PF2ENpc
import js.objects.Object


fun PF2ENpc.getKingdom(): KingdomData? =
    getAppFlag<PF2ENpc, KingdomData?>("kingdom-sheet")
        ?.let(::deepClone)

suspend fun PF2ENpc.setKingdom(data: KingdomData) {
    setAppFlag("kingdom-sheet", data)
}

fun PF2ENpc.getStructureData(): Structure? =
    getAppFlag("structureData")

fun PF2ENpc.getParsedStructureData(): StructureData? {
    val data = getStructureData()
    if (data != null) {
        if (Object.hasOwn(data, "ref")) {
            val ref = data.asDynamic()["ref"]
            return structures.find { it.name == ref }
        } else {
            @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
            return data as StructureData
        }
    }
    return null
}

suspend fun PF2ENpc.setStructureData(data: Structure) {
    setAppFlag("structureData", data)
}

suspend fun PF2ENpc.unsetStructureData() {
    unsetAppFlag("structureData")
}