package at.posselt.pfrpg2e.weather

import at.posselt.pfrpg2e.data.regions.RandomWeatherEvent
import at.posselt.pfrpg2e.data.regions.RandomWeatherEventTable

/**
 * Random Weather Events table data from KCG p.122.
 * Loaded from weather-events.json bundled resource.
 *
 * Source: Kingmaker Companion Guide, page 122.
 * This provides structured data for the d20 random weather events table,
 * enabling programmatic lookup without relying solely on Foundry roll tables.
 */
external interface WeatherEventJsonEntry {
    val id: String
    val name: String
    val level: Int
    val levelAlt: Int?
    val rollStart: Int
    val rollEnd: Int
}

external interface WeatherEventJsonData {
    val source: String
    val page: Int
    val dc: Int
    val events: Array<WeatherEventJsonEntry>
}

@JsModule("./weather-events.json")
private external val weatherEventJsonData: WeatherEventJsonData

/**
 * Convert the raw JS weather events JSON to typed RandomWeatherEventTable.
 */
fun loadRandomWeatherEventTable(): RandomWeatherEventTable {
    return RandomWeatherEventTable(
        source = weatherEventJsonData.source,
        page = weatherEventJsonData.page,
        dc = weatherEventJsonData.dc,
        events = weatherEventJsonData.events.map { entry ->
            RandomWeatherEvent(
                id = entry.id,
                name = entry.name,
                level = entry.level,
                levelAlt = entry.levelAlt,
                rollStart = entry.rollStart,
                rollEnd = entry.rollEnd,
            )
        }.toList()
    )
}
