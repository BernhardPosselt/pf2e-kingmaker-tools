package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.kingdom.KingdomData
import com.foundryvtt.core.Game

class Migration11 : Migration(11) {
    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        kingdom.modifiers.forEach { m ->
            val activities = m.activities
            if (activities != null && activities.contains("evangelize-the-dead")) {
                m.activities = activities.filter { it != "evangelize-the-dead" }.toTypedArray() + "evangelize-the-end"
            }
        }
    }
}