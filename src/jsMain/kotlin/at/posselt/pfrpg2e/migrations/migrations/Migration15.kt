package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.kingdom.structures.StructureRef
import at.posselt.pfrpg2e.kingdom.structures.getRawStructureData
import at.posselt.pfrpg2e.kingdom.structures.isStructure
import at.posselt.pfrpg2e.kingdom.structures.isStructureRef
import at.posselt.pfrpg2e.kingdom.structures.setStructureData
import at.posselt.pfrpg2e.kingdom.structures.structures
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actor.PF2ENpc


class Migration15 : Migration(15) {
    override suspend fun migrateOther(game: Game) {
        val structuresByName = structures.associateBy { it.name }
        game.actors
            .contents
            .filterIsInstance<PF2ENpc>()
            .filter { it.isStructure() }
            .forEach {
                val data = it.getRawStructureData().unsafeCast<AnyObject?>()
                if (data != null && isStructureRef(data)) {
                    val name = data.ref
                    val id = structuresByName[name]?.id
                    if (id != null) {
                        it.setStructureData(StructureRef(ref=id))
                    }
                }
            }
    }
}