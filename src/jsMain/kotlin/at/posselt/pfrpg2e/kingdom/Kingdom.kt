package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.kingdom.structures.RawStructure
import at.posselt.pfrpg2e.utils.getAppFlag
import at.posselt.pfrpg2e.utils.setAppFlag
import at.posselt.pfrpg2e.utils.unsetAppFlag
import com.foundryvtt.core.utils.deepClone
import com.foundryvtt.pf2e.actor.PF2ENpc

fun PF2ENpc.getKingdom(): KingdomData? =
    getAppFlag<PF2ENpc, KingdomData?>("kingdom-sheet")
        ?.let(::deepClone)

suspend fun PF2ENpc.setKingdom(data: KingdomData) {
    setAppFlag("kingdom-sheet", data)
}

fun PF2ENpc.getRawStructureData(): RawStructure? =
    getAppFlag("structureData")

suspend fun PF2ENpc.setStructureData(data: RawStructure) {
    setAppFlag("structureData", data)
}

suspend fun PF2ENpc.unsetStructureData() {
    unsetAppFlag("structureData")
}