package at.posselt.pfrpg2e.weather

import at.posselt.pfrpg2e.data.regions.WeatherEffect
import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.getPF2EWorldTime
import at.posselt.pfrpg2e.utils.isFirstGM
import com.foundryvtt.core.Game
import kotlinx.datetime.*

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


suspend fun setWeather(game: Game, weatherEffect: WeatherEffect) {
    game.settings.pfrpg2eKingdomCampingWeather.setCurrentWeatherFx(weatherEffect.toCamelCase())
    syncWeather(game)
}

fun dayHasChanged(game: Game, deltaInSeconds: Int): Boolean {
    val now = game.getPF2EWorldTime()
    val beforeUpdate = now.toInstant(TimeZone.UTC)
        .minus(deltaInSeconds, DateTimeUnit.SECOND)
        .toLocalDateTime(TimeZone.UTC)
    return now.dayOfMonth != beforeUpdate.dayOfMonth
            || now.month != beforeUpdate.month
            || now.year != beforeUpdate.year
}