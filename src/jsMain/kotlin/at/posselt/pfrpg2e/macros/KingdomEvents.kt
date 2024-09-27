package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.utils.rollWithCompendiumFallback
import com.foundryvtt.core.Game

suspend fun rollEvent(game: Game, tableName: String) {
    val rollMode = game.settings.pfrpg2eKingdomCampingWeather.getKingdomEventRollMode()
    game.rollWithCompendiumFallback(
        tableName = tableName,
        rollMode = rollMode,
    )
}

suspend fun rollKingdomEventMacro(game: Game) {
    val tableName = game.settings.pfrpg2eKingdomCampingWeather.getKingdomEventsTable()
    rollEvent(game, tableName)
}