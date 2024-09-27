package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.app.prompt
import at.posselt.pfrpg2e.weather.SceneWeatherSettings
import at.posselt.pfrpg2e.weather.getWeatherSettings
import at.posselt.pfrpg2e.weather.setWeatherSettings
import com.foundryvtt.core.documents.Scene
import js.objects.recordOf


suspend fun sceneWeatherSettingsMacro(scene: Scene) {
    val settings = scene.getWeatherSettings()
    prompt<SceneWeatherSettings, Unit>(
        title = "Scene Weather Settings",
        templatePath = "components/forms/form.hbs",
        templateContext = recordOf(
            "formRows" to formContext(
                CheckboxInput(
                    name = "syncWeather",
                    label = "Sync Weather",
                    value = settings.syncWeather,
                    help = "If enabled, changes weather on this scene to the current value"
                ),
                CheckboxInput(
                    name = "syncWeatherPlaylist",
                    label = "Sync Weather Playlist",
                    value = settings.syncWeatherPlaylist,
                    help = "If enabled, plays the current weather effect playlist"
                ),
            )
        )
    ) {
        scene.setWeatherSettings(it)
    }
}