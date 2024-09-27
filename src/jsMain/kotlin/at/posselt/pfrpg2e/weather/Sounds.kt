package at.posselt.pfrpg2e.weather

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.actor.isKingmakerInstalled
import at.posselt.pfrpg2e.data.regions.WeatherEffect
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.awaitAll
import at.posselt.pfrpg2e.utils.typeSafeUpdate
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.Playlist
import com.foundryvtt.core.documents.PlaylistSound
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

private fun findWeatherPlaylist(game: Game, weatherEffect: WeatherEffect): Playlist? =
    game.playlists.getName("weather.${weatherEffect.toCamelCase()}")

private fun findWeatherSoundByName(game: Game, playlistId: String, soundName: String): PlaylistSound? =
    game.playlists.find { it.id == playlistId }
        ?.sounds
        ?.getName(soundName)


/**
 * Retrieves a sound from the official Kingmaker -> SFX -> Loops playlist
 */
private fun findKingmakerWeatherSound(game: Game, weatherEffect: WeatherEffect): PlaylistSound? {
    val id = Config.kingmakerModule.weather.playlistId
    return if (game.isKingmakerInstalled) {
        when (weatherEffect) {
            WeatherEffect.SNOW -> findWeatherSoundByName(game, id, "Cold Wind")
            WeatherEffect.RAIN -> findWeatherSoundByName(game, id, "Rain")
            WeatherEffect.RAIN_STORM -> findWeatherSoundByName(game, id, "Thunderstorm")
            WeatherEffect.BLIZZARD -> findWeatherSoundByName(game, id, "Colder Wind")
            else -> null
        }
    } else null
}

private fun findPlayingWeatherPlaylists(game: Game, weatherEffects: List<WeatherEffect>) =
    weatherEffects.mapNotNull { findWeatherPlaylist(game, it) }
        .filter { it.playing }

private fun findPlayingWeatherSounds(game: Game, weatherEffects: List<WeatherEffect>) =
    weatherEffects.mapNotNull { findKingmakerWeatherSound(game, it) }
        .filter { it.playing }

private suspend fun play(game: Game, weatherEffect: WeatherEffect) {
    val playlist = findWeatherPlaylist(game, weatherEffect)
    if (playlist != null) {
        playlist.playAll().await()
    } else {
        findKingmakerWeatherSound(game, weatherEffect)
            ?.typeSafeUpdate { playing = true }
    }
}

private suspend fun stopNonCurrentlyPlaying(game: Game, currentWeatherEffect: WeatherEffect) = coroutineScope {
    val effectsToStop = WeatherEffect.entries.filter { it != currentWeatherEffect }
    val playlists = async {
        findPlayingWeatherPlaylists(game, effectsToStop)
            .map { it.stopAll() }
            .awaitAll()
    }
    val sounds = async {
        findPlayingWeatherSounds(game, effectsToStop)
            .map { async { it.typeSafeUpdate { playing = false } } }
            .awaitAll()
    }
    playlists.await()
    sounds.await()
}

suspend fun changeSoundTo(game: Game, weatherEffect: WeatherEffect) {
    stopNonCurrentlyPlaying(game, weatherEffect)
    if (weatherEffect != WeatherEffect.NONE) {
        play(game, weatherEffect)
    }
}