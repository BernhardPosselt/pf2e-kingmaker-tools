package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.structures.StructureActor
import at.posselt.pfrpg2e.kingdom.structures.isStructure
import at.posselt.pfrpg2e.kingdom.structures.parseStructure
import at.posselt.pfrpg2e.utils.typeSafeUpdate
import com.foundryvtt.core.Game

class Migration18 : Migration(18, showUpgradingNotices = true) {
    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        kingdom.settings.settlementsGenerateRd = false
        kingdom.settings.partialStructureConstruction = false
    }

    override suspend fun migrateOther(game: Game) {
        game.actors.contents
            .asSequence()
            .filterIsInstance<StructureActor>()
            .filter { it.isStructure() }
            .forEach {
                val structure = it.parseStructure()
                val rpCost = structure?.construction?.rp ?: 0
                if (rpCost > 0) {
                    it.typeSafeUpdate {
                        system.attributes.hp.max = rpCost
                    }
                    it.typeSafeUpdate {
                        system.attributes.hp.value = rpCost
                    }
                }
            }
    }
}