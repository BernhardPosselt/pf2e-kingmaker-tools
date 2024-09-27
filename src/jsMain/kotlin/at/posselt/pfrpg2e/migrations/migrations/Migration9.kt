package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.recoverArmyIds
import com.foundryvtt.core.Game

class Migration9 : Migration(9) {
    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        kingdom.modifiers.forEach { m ->
            val activities = m.activities
            if (activities != null && activities.contains("recover-army")) {
                m.activities = activities.filter { it != "recover-army" }.toTypedArray() + recoverArmyIds
            }
        }
    }
}