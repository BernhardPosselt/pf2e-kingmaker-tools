package at.posselt.pfrpg2e.data.regions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WeatherEventTest {

    private val testTable = RandomWeatherEventTable(
        source = "Test",
        page = 122,
        dc = 17,
        events = listOf(
            RandomWeatherEvent(id = "fog", name = "Fog", level = 0, rollStart = 1, rollEnd = 3),
            RandomWeatherEvent(id = "heavy-downpour", name = "Heavy Downpour", level = 0, rollStart = 4, rollEnd = 7),
            RandomWeatherEvent(id = "cold-snap", name = "Cold Snap", level = 1, rollStart = 8, rollEnd = 9),
            RandomWeatherEvent(id = "windstorm", name = "Windstorm", level = 1, rollStart = 10, rollEnd = 12),
            RandomWeatherEvent(id = "severe-hailstorm", name = "Severe Hailstorm", level = 2, rollStart = 13, rollEnd = 13),
            RandomWeatherEvent(id = "blizzard", name = "Blizzard", level = 6, rollStart = 14, rollEnd = 14),
            RandomWeatherEvent(id = "supernatural-storm", name = "Supernatural Storm", level = 6, rollStart = 15, rollEnd = 15),
            RandomWeatherEvent(id = "flash-flood", name = "Flash Flood", level = 7, rollStart = 16, rollEnd = 16),
            RandomWeatherEvent(id = "wildfire", name = "Wildfire", level = 4, levelAlt = 10, rollStart = 17, rollEnd = 17),
            RandomWeatherEvent(id = "subsidence", name = "Subsidence", level = 5, levelAlt = 12, rollStart = 18, rollEnd = 18),
            RandomWeatherEvent(id = "thunderstorm", name = "Thunderstorm", level = 7, levelAlt = 13, rollStart = 19, rollEnd = 19),
            RandomWeatherEvent(id = "tornado", name = "Tornado", level = 12, levelAlt = 17, rollStart = 20, rollEnd = 20),
        )
    )

    // --- Table data shape ---

    @Test
    fun tableHasCorrectNumberOfEvents() {
        assertEquals(12, testTable.events.size)
    }

    @Test
    fun tableRollRangeCovers1To20() {
        assertEquals(1..20, testTable.rollRange)
    }

    @Test
    fun tableRollRangesAreContiguous() {
        val sorted = testTable.events.sortedBy { it.rollStart }
        // First entry starts at 1
        assertEquals(1, sorted.first().rollStart)
        // Last entry ends at 20
        assertEquals(20, sorted.last().rollEnd)
        // Each entry's rollStart is exactly one after the previous entry's rollEnd
        for (i in 1 until sorted.size) {
            assertEquals(
                sorted[i - 1].rollEnd + 1,
                sorted[i].rollStart,
                "Gap between entries ${sorted[i - 1].id} (ends ${sorted[i - 1].rollEnd}) and ${sorted[i].id} (starts ${sorted[i].rollStart})"
            )
        }
    }

    @Test
    fun tableHasNoGapsOrOverlaps() {
        val coverage = BooleanArray(21) // index 1..20
        for (event in testTable.events) {
            for (r in event.rollStart..event.rollEnd) {
                assertFalse(coverage[r], "Roll $r is covered by multiple events (overlap)")
                coverage[r] = true
            }
        }
        for (r in 1..20) {
            assertTrue(coverage[r], "Roll $r is not covered by any event (gap)")
        }
    }

    @Test
    fun fourEventsHaveAlternateLevel() {
        val withAlt = testTable.events.filter { it.hasAlternateLevel }
        assertEquals(4, withAlt.size)
        assertEquals(setOf("wildfire", "subsidence", "thunderstorm", "tornado"), withAlt.map { it.id }.toSet())
    }

    @Test
    fun eventLevelsMatchTableStructure() {
        assertEquals(0, testTable.events.find { it.id == "fog" }!!.level)
        assertEquals(0, testTable.events.find { it.id == "heavy-downpour" }!!.level)
        assertEquals(1, testTable.events.find { it.id == "cold-snap" }!!.level)
        assertEquals(1, testTable.events.find { it.id == "windstorm" }!!.level)
        assertEquals(2, testTable.events.find { it.id == "severe-hailstorm" }!!.level)
        assertEquals(6, testTable.events.find { it.id == "blizzard" }!!.level)
        assertEquals(6, testTable.events.find { it.id == "supernatural-storm" }!!.level)
        assertEquals(7, testTable.events.find { it.id == "flash-flood" }!!.level)
        assertEquals(4, testTable.events.find { it.id == "wildfire" }!!.level)
        assertEquals(10, testTable.events.find { it.id == "wildfire" }!!.levelAlt)
        assertEquals(5, testTable.events.find { it.id == "subsidence" }!!.level)
        assertEquals(12, testTable.events.find { it.id == "subsidence" }!!.levelAlt)
        assertEquals(7, testTable.events.find { it.id == "thunderstorm" }!!.level)
        assertEquals(13, testTable.events.find { it.id == "thunderstorm" }!!.levelAlt)
        assertEquals(12, testTable.events.find { it.id == "tornado" }!!.level)
        assertEquals(17, testTable.events.find { it.id == "tornado" }!!.levelAlt)
    }

    @Test
    fun maxLevelUsesAlternateWhenPresent() {
        val wildfire = testTable.events.find { it.id == "wildfire" }!!
        assertEquals(4, wildfire.minLevel)
        assertEquals(10, wildfire.maxLevel)
        val fog = testTable.events.find { it.id == "fog" }!!
        assertEquals(0, fog.minLevel)
        assertEquals(0, fog.maxLevel)
    }

    // --- Lookup correctness ---

    @Test
    fun lookupReturnsCorrectEventForEachRoll() {
        val expected = mapOf(
            1 to "fog", 2 to "fog", 3 to "fog",
            4 to "heavy-downpour", 5 to "heavy-downpour", 6 to "heavy-downpour", 7 to "heavy-downpour",
            8 to "cold-snap", 9 to "cold-snap",
            10 to "windstorm", 11 to "windstorm", 12 to "windstorm",
            13 to "severe-hailstorm",
            14 to "blizzard",
            15 to "supernatural-storm",
            16 to "flash-flood",
            17 to "wildfire",
            18 to "subsidence",
            19 to "thunderstorm",
            20 to "tornado",
        )
        for ((roll, expectedId) in expected) {
            assertEquals(expectedId, testTable.lookup(roll)?.id, "Roll $roll should map to $expectedId")
        }
    }

    @Test
    fun lookupReturnsNullForOutOfRangeRolls() {
        assertNull(testTable.lookup(0))
        assertNull(testTable.lookup(21))
        assertNull(testTable.lookup(-1))
    }

    // --- Level applicability ---

    @Test
    fun isApplicableReturnsTrueForLowLevelParty() {
        // Party level 0, range 4 -> max allowed level 4
        assertTrue(testTable.isApplicable(testTable.events.find { it.id == "fog" }!!, 0, 4))
        assertTrue(testTable.isApplicable(testTable.events.find { it.id == "heavy-downpour" }!!, 0, 4))
        assertTrue(testTable.isApplicable(testTable.events.find { it.id == "cold-snap" }!!, 0, 4))
        assertTrue(testTable.isApplicable(testTable.events.find { it.id == "windstorm" }!!, 0, 4))
        assertTrue(testTable.isApplicable(testTable.events.find { it.id == "severe-hailstorm" }!!, 0, 4))
    }

    @Test
    fun isApplicableReturnsFalseWhenLevelExceedsRange() {
        // Party level 0, range 4 -> max allowed level 4
        assertFalse(testTable.isApplicable(testTable.events.find { it.id == "blizzard" }!!, 0, 4))
        assertFalse(testTable.isApplicable(testTable.events.find { it.id == "flash-flood" }!!, 0, 4))
        assertFalse(testTable.isApplicable(testTable.events.find { it.id == "tornado" }!!, 0, 4))
    }

    @Test
    fun isApplicableUsesMinLevelNotMaxLevel() {
        // Wildfire: minLevel=4, maxLevel=10
        // Party level 2, range 4 -> max allowed level 6
        // minLevel 4 <= 6, so should be applicable
        assertTrue(testTable.isApplicable(testTable.events.find { it.id == "wildfire" }!!, 2, 4))
    }

    @Test
    fun isAlternateLevelApplicableReturnsTrueWhenAltInRange() {
        // Wildfire: levelAlt=10
        // Party level 6, range 4 -> max allowed level 10
        assertTrue(testTable.isAlternateLevelApplicable(testTable.events.find { it.id == "wildfire" }!!, 6, 4))
    }

    @Test
    fun isAlternateLevelApplicableReturnsFalseWhenAltExceedsRange() {
        // Wildfire: levelAlt=10
        // Party level 5, range 4 -> max allowed level 9
        assertFalse(testTable.isAlternateLevelApplicable(testTable.events.find { it.id == "wildfire" }!!, 5, 4))
    }

    @Test
    fun isAlternateLevelApplicableReturnsFalseForEventsWithoutAlt() {
        assertFalse(testTable.isAlternateLevelApplicable(testTable.events.find { it.id == "fog" }!!, 10, 4))
    }

    // --- resolveRandomWeatherEvent ---

    @Test
    fun resolveReturnsEventWhenInRange() {
        val result = resolveRandomWeatherEvent(roll = 1, partyLevel = 5, maxRange = 4, table = testTable)
        assertTrue(result is WeatherEventResult.Event)
        assertEquals("fog", result.event.id)
    }

    @Test
    fun resolveReturnsRerollWhenLevelTooHigh() {
        // Roll 20 = Tornado level 12
        // Party level 1, range 4 -> max allowed 5
        val result = resolveRandomWeatherEvent(roll = 20, partyLevel = 1, maxRange = 4, table = testTable)
        assertTrue(result is WeatherEventResult.Reroll)
    }

    @Test
    fun resolveReturnsNoEventForOutOfRangeRoll() {
        val result = resolveRandomWeatherEvent(roll = 0, partyLevel = 5, maxRange = 4, table = testTable)
        assertTrue(result is WeatherEventResult.NoEvent)
    }

    @Test
    fun resolveReturnsEventForHighestLevelEventWhenPartyStrongEnough() {
        // Roll 20 = Tornado level 12
        // Party level 12, range 4 -> max allowed 16
        val result = resolveRandomWeatherEvent(roll = 20, partyLevel = 12, maxRange = 4, table = testTable)
        assertTrue(result is WeatherEventResult.Event)
        assertEquals("tornado", result.event.id)
    }

    @Test
    fun resolveRerollBoundaryIsCorrect() {
        // Roll 14 = Blizzard level 6
        // Party level 2, range 4 -> max allowed 6 -> should succeed (level 6 <= 6)
        val atBoundary = resolveRandomWeatherEvent(roll = 14, partyLevel = 2, maxRange = 4, table = testTable)
        assertTrue(atBoundary is WeatherEventResult.Event)

        // Party level 1, range 4 -> max allowed 5 -> should reroll (level 6 > 5)
        val belowBoundary = resolveRandomWeatherEvent(roll = 14, partyLevel = 1, maxRange = 4, table = testTable)
        assertTrue(belowBoundary is WeatherEventResult.Reroll)
    }
}
