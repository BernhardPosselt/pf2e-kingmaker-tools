package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.kingdom.KingdomData
import com.foundryvtt.core.Game

class Migration18: Migration(18, showUpgradingNotices = true) {
    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        kingdom.settings.settlementsGenerateRd = false
    }
}