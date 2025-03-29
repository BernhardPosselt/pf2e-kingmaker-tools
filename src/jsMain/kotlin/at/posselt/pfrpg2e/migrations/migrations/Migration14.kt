package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.kingdom.KingdomData
import com.foundryvtt.core.Game

class Migration14 : Migration(14) {
    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        kingdom.homebrewKingdomEvents = emptyArray()
        kingdom.kingdomEventBlacklist = emptyArray()
        kingdom.activeKingdomEvents = emptyArray()
        // TODO migrate ongoing events and delete property
        // TODO print non migrateable events to chat
    }

    override suspend fun migrateOther(game: Game) {

    }
}