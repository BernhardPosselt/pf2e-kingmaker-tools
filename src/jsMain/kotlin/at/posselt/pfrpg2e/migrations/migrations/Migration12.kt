package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.kingdom.KingdomData
import com.foundryvtt.core.Game

class Migration12 : Migration(12) {
    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        kingdom.activityBlacklist = kingdom.activityBlacklist + arrayOf("focused-attention-vk", "celebrate-holiday-vk", "retrain-vk", "fortify-hex-vk", "clear-hex-vk")
    }
}