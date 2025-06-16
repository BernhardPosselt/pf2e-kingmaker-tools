package at.posselt.pfrpg2e.weather

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.camping.getActiveCamping
import at.posselt.pfrpg2e.camping.getAveragePartyLevel
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.data.regions.Climate
import at.posselt.pfrpg2e.data.regions.Month
import at.posselt.pfrpg2e.data.regions.Season
import at.posselt.pfrpg2e.data.regions.WeatherEffect
import at.posselt.pfrpg2e.data.regions.WeatherType
import at.posselt.pfrpg2e.data.regions.findWeatherType
import at.posselt.pfrpg2e.data.regions.getMonth
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.DieValue
import at.posselt.pfrpg2e.utils.buildUuid
import at.posselt.pfrpg2e.utils.d20Check
import at.posselt.pfrpg2e.utils.getCurrentMonth
import at.posselt.pfrpg2e.utils.postChatMessage
import at.posselt.pfrpg2e.utils.rollWithCompendiumFallback
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.TableMessageOptions
import js.objects.recordOf

private val hazardLevelRegex = "\\(Hazard (?<level>\\d+)\\+?\\)".toRegex(RegexOption.IGNORE_CASE)

/**
 * All Weather Events results from the roll table should include a "(Hazard X)"
 * text part where X is the level of the Hazard
 */
private fun parseHazardLevel(eventName: String): Int? =
    hazardLevelRegex.find(eventName)?.let {
        it.groups["level"]?.value?.toIntOrNull()
    }

private suspend fun rollWeatherEvent(
    game: Game,
    averagePartyLevel: Int,
    maximumRange: Int,
    rollMode: RollMode,
) {
    val tableName = "Weather Events"
    val uuid = game.tables.find { it.name == tableName }?.uuid
    val result = game.rollWithCompendiumFallback(
        uuid = uuid,
        compendiumUuid = "Compendium.pf2e-kingmaker-tools.kingmaker-tools-rolltables.RollTable.LFhri3HZpH7j8QpV",
        rollMode = rollMode,
        displayChat = false
    )
    if (result == null) {
        console.error("Could neither find table with name '$tableName' in world or compendium '${Config.rollTables.compendium}'")
        return
    }
    val (table, draw) = result
    val text = draw.results.firstOrNull()?.text
    val hazardLevel = text?.let(::parseHazardLevel)
    if (hazardLevel == null) {
        console.error("Can not parse hazard level from weather events table result '$text', add a (Hazard X) part where X is the level of the hazard")
        return
    }
    if (hazardLevel > averagePartyLevel + maximumRange) {
        console.log("Re-Rolling event, level $hazardLevel is more than $maximumRange higher than party level $averagePartyLevel")
        rollWeatherEvent(game, averagePartyLevel, maximumRange, rollMode)
    } else {
        table.toMessage(
            draw.results,
            TableMessageOptions(roll = draw.roll, messageOptions = recordOf("rollMode" to rollMode.toCamelCase())),
        )
    }
}

private suspend fun rollWeather(
    game: Game,
    month: Month,
    climate: Array<Climate>,
    averagePartyLevel: Int,
    maximumRange: Int,
    rollMode: RollMode,
) {
    climate.find { it.month == month }
        ?.let {
            // 1. roll flat checks
            val checkPrecipitation = it.precipitationDc?.let { precipitationDc ->
                d20Check(
                    precipitationDc,
                    flavor = t("weather.checkPrecipitation", recordOf("dc" to precipitationDc)),
                    rollMode = rollMode,
                )
            }
            val checkCold = it.coldDc?.let { coldDc ->
                d20Check(
                    coldDc,
                    flavor = t("weather.checkingCold", recordOf("dc" to coldDc)),
                    rollMode = rollMode,
                )
            }
            it.weatherEventDc?.let { weatherEventDc ->
                // 2. check if weather events happen
                val checkEvent = d20Check(
                    weatherEventDc,
                    flavor = t("weather.checkingWeatherEvent", recordOf("dc" to weatherEventDc)),
                    rollMode = rollMode,
                )
                val checkSecondEvent = checkEvent.dieValue
                    .takeIf(DieValue::isNat20)
                    ?.run {
                        d20Check(
                            weatherEventDc,
                            flavor = t("weather.checkingSecondWeatherEvent", recordOf("dc" to weatherEventDc)),
                            rollMode = rollMode,
                        )
                    }
                // 3. roll weather events
                if (checkEvent.degreeOfSuccess.succeeded()) rollWeatherEvent(
                    game,
                    averagePartyLevel,
                    maximumRange,
                    rollMode,
                )
                if (checkSecondEvent?.degreeOfSuccess?.succeeded() == true) rollWeatherEvent(
                    game,
                    averagePartyLevel,
                    maximumRange,
                    rollMode,
                )
            }
            // 4. post weather result to chat
            val type = findWeatherType(
                isCold = checkCold?.degreeOfSuccess?.succeeded() == true,
                hasPrecipitation = checkPrecipitation?.degreeOfSuccess?.succeeded() == true,
            )
            val weatherEffect = when (type) {
                WeatherType.COLD -> {
                    postChatMessage(t("weather.cold"))
                    WeatherEffect.SUNNY
                }

                WeatherType.SNOWY -> {
                    postChatMessage(t("weather.snow"))
                    postLightPrecipitationEffect()
                    WeatherEffect.SNOW
                }

                WeatherType.RAINY -> {
                    postChatMessage(t("weather.rainy"))
                    postLightPrecipitationEffect()
                    WeatherEffect.RAIN
                }

                WeatherType.SUNNY -> {
                    postChatMessage(t("weather.sunny"))
                    WeatherEffect.SUNNY
                }
            }
            // 5. set new weather
            setWeather(game, weatherEffect)
        }
}

private suspend fun postLightPrecipitationEffect() {
    val uuid = "Compendium.pf2e-kingmaker-tools.kingmaker-tools-weather-effects.Item.YRCfmtAtySyeBFwt"
    postChatMessage(buildUuid(uuid), isHtml = true)
}

suspend fun rollWeather(game: Game) {
    val settings = game.settings.pfrpg2eKingdomCampingWeather
    val climateSettings = settings.getClimateSettings()
    val climate = climateSettings.months.mapIndexed { index, climateSetting ->
        Climate(
            month = getMonth(index),
            season = fromCamelCase<Season>(climateSetting.season)!!,
            coldDc = climateSetting.coldDc,
            precipitationDc = climateSetting.precipitationDc,
            weatherEventDc = climateSetting.weatherEventDc,
        )
    }.toTypedArray()
    val camping = game.getActiveCamping()
    if (camping != null) {
        rollWeather(
            game = game,
            month = game.getCurrentMonth(),
            climate = climate,
            averagePartyLevel = camping.getAveragePartyLevel(),
            maximumRange = settings.getWeatherHazardRange(),
            rollMode = settings.getWeatherRollMode(),
        )
    }
}