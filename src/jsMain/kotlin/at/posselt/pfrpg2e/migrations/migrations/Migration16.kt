package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.kingdom.KingdomData
import com.foundryvtt.core.Game

class Migration16 : Migration(16) {
    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        kingdom.homebrewFeats = kingdom.homebrewFeats.map {
            it.copy(rollOptions = it.asDynamic().flags)
        }.toTypedArray()
        kingdom.modifiers = kingdom.modifiers.filter {
            it.valueExpression == null
        }.toTypedArray()
        kingdom.settings.eventDc = 16
        kingdom.settings.eventDcStep = 5
        kingdom.settings.cultEventDc = 20
        kingdom.settings.cultEventDcStep = 2
    }
}