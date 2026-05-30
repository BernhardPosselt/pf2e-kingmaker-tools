package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.RawCouncilCooldowns
import com.foundryvtt.core.Game

class Migration22 : Migration(22, false) {

    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        kingdom.settings.enableCouncilMissions = false
        kingdom.councilCooldowns = RawCouncilCooldowns(
            audit = 0,
            scrying = 0,
            lockdown = 0,
            feast = 0,
        )
    }
}
