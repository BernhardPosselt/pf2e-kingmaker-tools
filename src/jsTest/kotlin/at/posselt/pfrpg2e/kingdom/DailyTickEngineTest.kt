package at.posselt.pfrpg2e.kingdom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DailyTickEngineTest {

    // ── Travel ETA ticking ─────────────────────────────────────────────

    @Test
    fun testEtaDecrementsByOneDay() {
        val result = DailyTickEngine.tickTravelEta(eta = 3, days = 1)
        assertEquals(2, result.newEta)
        assertTrue(result.traveling)
        assertFalse(result.arrived)
    }

    @Test
    fun testEtaDecrementsByMultipleDays() {
        val result = DailyTickEngine.tickTravelEta(eta = 5, days = 3)
        assertEquals(2, result.newEta)
        assertTrue(result.traveling)
        assertFalse(result.arrived)
    }

    @Test
    fun testEtaReachingZeroArrives() {
        val result = DailyTickEngine.tickTravelEta(eta = 1, days = 1)
        assertNull(result.newEta, "Arrived companions have no remaining ETA")
        assertFalse(result.traveling)
        assertTrue(result.arrived)
    }

    @Test
    fun testEtaOvershootArrives() {
        // Advancing more days than the remaining ETA still arrives exactly once.
        val result = DailyTickEngine.tickTravelEta(eta = 2, days = 7)
        assertNull(result.newEta)
        assertFalse(result.traveling)
        assertTrue(result.arrived)
    }

    @Test
    fun testNullEtaIsNotTraveling() {
        val result = DailyTickEngine.tickTravelEta(eta = null, days = 1)
        assertNull(result.newEta)
        assertFalse(result.traveling)
        assertFalse(result.arrived, "No ETA means there is nothing to arrive at")
    }

    @Test
    fun testZeroOrNegativeDaysCoercedToOne() {
        // Defensive: a non-positive day count should still advance a single day
        // rather than freezing or moving travel backwards.
        val result = DailyTickEngine.tickTravelEta(eta = 3, days = 0)
        assertEquals(2, result.newEta)
        assertTrue(result.traveling)
    }

    @Test
    fun testDefaultDaysIsOne() {
        val result = DailyTickEngine.tickTravelEta(eta = 4)
        assertEquals(3, result.newEta)
    }

    @Test
    fun testSequentialTravelTicksReachArrival() {
        var eta: Int? = 3
        // day 1
        var result = DailyTickEngine.tickTravelEta(eta, days = 1)
        eta = result.newEta
        assertEquals(2, eta)
        assertFalse(result.arrived)
        // day 2
        result = DailyTickEngine.tickTravelEta(eta, days = 1)
        eta = result.newEta
        assertEquals(1, eta)
        assertFalse(result.arrived)
        // day 3 — arrival
        result = DailyTickEngine.tickTravelEta(eta, days = 1)
        assertNull(result.newEta)
        assertTrue(result.arrived)
        assertFalse(result.traveling)
    }
}
