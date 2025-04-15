package at.posselt.pfrpg2e.utils

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import com.foundryvtt.core.Game
import js.objects.recordOf

fun registerTokenMappings(game: Game) {
    // load custom token mappings if kingmaker module isn't installed
    if (game.modules.get("pf2e-kingmaker")?.active != true
        && game.settings.pfrpg2eKingdomCampingWeather.getEnableTokenMapping()
    ) {
        val data = recordOf(
            "flags" to recordOf(
                Config.moduleId to recordOf(
                    "pf2e-art" to "modules/${Config.moduleId}/token-map.json"
                )
            )
        )
        game.modules.get(Config.moduleId)
            ?.updateSource(data)
    }
}