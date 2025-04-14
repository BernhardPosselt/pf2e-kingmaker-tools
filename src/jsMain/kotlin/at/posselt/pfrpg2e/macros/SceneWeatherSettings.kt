package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.app.prompt
import at.posselt.pfrpg2e.utils.t
import at.posselt.pfrpg2e.weather.SceneWeatherSettings
import at.posselt.pfrpg2e.weather.getWeatherSettings
import at.posselt.pfrpg2e.weather.setWeatherSettings
import com.foundryvtt.core.documents.Scene
import js.objects.recordOf


suspend fun sceneWeatherSettingsMacro(scene: Scene) {
    val settings = scene.getWeatherSettings()
    prompt<SceneWeatherSettings, Unit>(
        title = t("macros.sceneWeatherSettings.title"),
        templatePath = "components/forms/form.hbs",
        templateContext = recordOf(
            "formRows" to formContext(
                CheckboxInput(
                    name = "syncWeather",
                    label = t("macros.sceneWeatherSettings.syncWeather"),
                    value = settings.syncWeather,
                    help = t("macros.sceneWeatherSettings.syncWeatherHelp"),
                ),
                CheckboxInput(
                    name = "syncWeatherPlaylist",
                    label = t("macros.sceneWeatherSettings.syncWeatherPlaylist"),
                    value = settings.syncWeatherPlaylist,
                    help = t("macros.sceneWeatherSettings.syncWeatherPlaylistHelp"),
                ),
            )
        )
    ) {
        scene.setWeatherSettings(it)
    }
}