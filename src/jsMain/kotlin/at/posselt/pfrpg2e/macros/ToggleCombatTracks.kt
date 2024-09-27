package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import com.foundryvtt.core.Game

suspend fun toggleCombatTracksMacro(game: Game) {
    val settings = game.settings.pfrpg2eKingdomCampingWeather
    settings.setEnableCombatTracks(!settings.getEnableCombatTracks())
}