package at.posselt.pfrpg2e.data.regions

/**
 * Represents a single entry from the Random Weather Events table (KCG p.122).
 *
 * Source: Kingmaker Companion Guide, page 122, "Random Weather Events"
 * Trigger: DC 17 flat check at daily preparations; on success roll d20.
 * On natural 20: potential secondary event (GM chooses thematically linked).
 * Reroll any hazard > party level + [weatherHazardRange setting, default 4].
 *
 * Note: Weather event descriptions (mechanical effects) are not included here
 * as they require direct KCG text which cannot be reproduced. Only the table
 * structure (name, roll range, level) is captured as data.
 *
 * Some events have an alternative level (levelAlt) for GM choice.
 * Wildfire (4 or 10), Subsidence (5 or 12), Thunderstorm (7 or 13), Tornado (12 or 17).
 */
data class RandomWeatherEvent(
    val id: String,
    val name: String,
    val level: Int,
    val levelAlt: Int? = null,
    val rollStart: Int,
    val rollEnd: Int,
) {
    /** The minimum hazard level for this event (always applicable). */
    val minLevel: Int get() = level

    /** The maximum hazard level (for events with an alternate). Max of both levels. */
    val maxLevel: Int get() = maxOf(level, levelAlt ?: level)

    /** Whether this event has an alternative (higher) level the GM can choose. */
    val hasAlternateLevel: Boolean get() = levelAlt != null
}

/**
 * The complete Random Weather Events table from KCG p.122.
 * Provides lookup by d20 roll and level-based filtering.
 */
data class RandomWeatherEventTable(
    val source: String,
    val page: Int,
    val dc: Int,
    val events: List<RandomWeatherEvent>,
) {
    /**
     * Look up the weather event for a given d20 roll result.
     */
    fun lookup(roll: Int): RandomWeatherEvent? =
        events.find { roll in it.rollStart..it.rollEnd }

    /**
     * Whether the event's hazard level is within the allowed range for the party.
     * Uses the minimum level for filtering (conservative: always allow lower).
     * For events with an alternate level, the GM can choose the higher level
     * if the party level allows it.
     */
    fun isApplicable(event: RandomWeatherEvent, partyLevel: Int, maxRange: Int): Boolean =
        event.minLevel <= partyLevel + maxRange

    /**
     * Whether the event's alternate (higher) level is within range.
     * Returns false if the event has no alternate level.
     */
    fun isAlternateLevelApplicable(event: RandomWeatherEvent, partyLevel: Int, maxRange: Int): Boolean =
        event.levelAlt != null && event.levelAlt <= partyLevel + maxRange

    val rollRange: IntRange get() {
        val min = events.minOf { it.rollStart }
        val max = events.maxOf { it.rollEnd }
        return min..max
    }
}

/**
 * Roll resolution result: either a valid event or a signal to re-roll.
 */
sealed class WeatherEventResult {
    data class Event(val event: RandomWeatherEvent) : WeatherEventResult()
    data object Reroll : WeatherEventResult()
    data object NoEvent : WeatherEventResult()
}

/**
 * Resolve a random weather event from the table for a given d20 roll.
 * If the rolled event's minimum level exceeds partyLevel + maxRange, returns Reroll.
 * If no event matches the roll, returns NoEvent.
 */
fun resolveRandomWeatherEvent(
    roll: Int,
    partyLevel: Int,
    maxRange: Int,
    table: RandomWeatherEventTable,
): WeatherEventResult {
    val event = table.lookup(roll) ?: return WeatherEventResult.NoEvent
    return if (table.isApplicable(event, partyLevel, maxRange)) {
        WeatherEventResult.Event(event)
    } else {
        WeatherEventResult.Reroll
    }
}
