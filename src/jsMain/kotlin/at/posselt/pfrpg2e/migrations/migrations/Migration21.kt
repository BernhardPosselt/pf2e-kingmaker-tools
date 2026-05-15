package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.kingdom.KingdomData
import com.foundryvtt.core.Game


class Migration21 : Migration(21) {

    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        kingdom.settings.capitalCanGrowOneSizeLarger = false
    }
}