package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.kingdom.KingdomData
import com.foundryvtt.core.Game

class Migration24 : Migration(24, false) {

    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        if (kingdom.companions == null) {
            kingdom.companions = emptyArray()
        }
    }
}
