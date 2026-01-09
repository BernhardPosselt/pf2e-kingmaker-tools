package at.posselt.pfrpg2e.weather

import at.posselt.pfrpg2e.data.regions.WeatherEffect
import at.posselt.pfrpg2e.data.regions.WeatherType
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.isFirstGM
import com.foundryvtt.core.Game

/**
 * Read the persisted weather effect name and sound and apply them
 */
suspend fun syncWeather(game: Game) {
    val settings = game.settings.pfrpg2eKingdomCampingWeather
    if (game.isFirstGM() && settings.getEnableWeather()) {
        val weather = getCurrentWeatherFx(settings)
        getScenesToSyncWeather(game)
            .filter { it.getWeatherSettings().syncWeather }
            .forEach { setSceneWeatherFx(it, weather) }
        if (settings.getEnableWeatherSoundFx()) {
            game.scenes.active
                ?.takeIf { it.getWeatherSettings().syncWeatherPlaylist }
                ?.let { changeSoundTo(game, weather) }
        }
    }
}


suspend fun setWeather(game: Game, weatherEffect: WeatherEffect, type: WeatherType) {
    game.settings.pfrpg2eKingdomCampingWeather.setCurrentWeatherFx(weatherEffect.toCamelCase())
    game.settings.pfrpg2eKingdomCampingWeather.setCurrentWeatherType(type.value)
    syncWeather(game)
}

fun Game.getCurrentWeatherType(): WeatherType =
    if (settings.pfrpg2eKingdomCampingWeather.getEnableWeather()) {
        fromCamelCase<WeatherType>(settings.pfrpg2eKingdomCampingWeather.getCurrentWeatherType())
            ?: WeatherType.SUNNY
    } else {
        WeatherType.SUNNY
    }
