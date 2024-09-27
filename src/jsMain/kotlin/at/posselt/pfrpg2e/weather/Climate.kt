package at.posselt.pfrpg2e.weather

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.actor.averagePartyLevel
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.data.regions.*
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.utils.*
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
    val tableName = Config.rollTables.weather
    val result = game.rollWithCompendiumFallback(
        tableName = tableName,
        fallbackName = tableName,
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
            TableMessageOptions(roll = draw.roll, messageOptions = recordOf("rollMode" to rollMode))
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
                    flavor = "Checking for Precipitation with DC ${precipitationDc}",
                    rollMode = rollMode,
                )
            }
            val checkCold = it.coldDc?.let { coldDc ->
                d20Check(
                    coldDc,
                    flavor = "Checking for Cold with DC $coldDc",
                    rollMode = rollMode,
                )
            }
            it.weatherEventDc?.let { weatherEventDc ->
                // 2. check if weather events happen
                val checkEvent = d20Check(
                    weatherEventDc,
                    flavor = "Checking for Weather Event with DC ${weatherEventDc}",
                    rollMode = rollMode,
                )
                val checkSecondEvent = checkEvent.dieValue
                    .takeIf(DieValue::isNat20)
                    ?.run {
                        d20Check(
                            weatherEventDc,
                            flavor = "Checking for Second Weather Event with DC ${weatherEventDc}",
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
                    postChatMessage("Weather: Cold")
                    WeatherEffect.SUNNY
                }

                WeatherType.SNOWY -> {
                    postChatMessage("Weather: Cold & Snowing")
                    WeatherEffect.SNOW
                }

                WeatherType.RAINY -> {
                    postChatMessage("Weather: Rainy")
                    WeatherEffect.RAIN
                }

                WeatherType.SUNNY -> {
                    postChatMessage("Weather: Sunny")
                    WeatherEffect.SUNNY
                }
            }
            // 5. set new weather
            setWeather(game, weatherEffect)
        }
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
    rollWeather(
        game = game,
        month = game.getCurrentMonth(),
        climate = climate,
        averagePartyLevel = game.averagePartyLevel(),
        maximumRange = settings.getWeatherHazardRange(),
        rollMode = settings.getWeatherRollMode(),
    )
}