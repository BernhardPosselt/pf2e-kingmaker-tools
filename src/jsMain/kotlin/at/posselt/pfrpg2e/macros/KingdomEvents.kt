package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.getKingdomActor
import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.utils.rollWithCompendiumFallback
import com.foundryvtt.core.Game


suspend fun rollKingdomEventMacro(game: Game) {
    val uuid = game.getKingdomActor()
        ?.getKingdom()
        ?.settings
        ?.kingdomEventsTable
    val rollMode = game.settings.pfrpg2eKingdomCampingWeather.getKingdomEventRollMode()
    game.rollWithCompendiumFallback(
        tableName = "Random Kingdom Events",
        rollMode = rollMode,
        uuid = uuid,
    )
}

suspend fun rollCultEventMacro(game: Game) {
    val uuid = game.getKingdomActor()
        ?.getKingdom()
        ?.settings
        ?.kingdomCultTable
    val rollMode = game.settings.pfrpg2eKingdomCampingWeather.getKingdomEventRollMode()
    game.rollWithCompendiumFallback(
        tableName = "Random Cult Events",
        rollMode = rollMode,
        uuid = uuid,
    )
}