package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.weather.syncWeather
import com.foundryvtt.core.Game

suspend fun toggleWeatherMacro(game: Game) {
    val isEnabled = game.settings.pfrpg2eKingdomCampingWeather.getEnableWeather()
    game.settings.pfrpg2eKingdomCampingWeather.setEnableWeather(!isEnabled)
    syncWeather(game)
}
