package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.Notes
import com.foundryvtt.core.Game

class Migration7 : Migration(7) {
    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        @Suppress("SENSELESS_COMPARISON")
        if (kingdom.notes == null) {
            kingdom.notes = Notes(
                public = "",
                gm = "",
            )
        }
    }
}