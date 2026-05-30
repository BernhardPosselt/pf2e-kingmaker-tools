package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.kingdom.KingdomData
import com.foundryvtt.core.Game

class Migration23 : Migration(23, false) {

    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        if (kingdom.quests == null) {
            kingdom.quests = emptyArray()
        }
    }
}
