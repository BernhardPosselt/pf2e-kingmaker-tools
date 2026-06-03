package at.posselt.pfrpg2e.camping

import js.objects.unsafeJso
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the persistent per-actor downtime budget on [CampingData]: hours are spent per roll,
 * accumulate, are never refunded by unassigning, and clamp to the 8-hour maximum.
 */
class CampingDowntimeHoursTest {

    private fun emptyCamping(): CampingData = unsafeJso {}

    @Test
    fun remainingIsFullWhenNothingSpent() {
        val camping = emptyCamping()
        assertEquals(8, camping.downtimeHoursRemaining("actor-1"))
    }

    @Test
    fun eachRollSpendsTwoHoursAndAccumulates() {
        val camping = emptyCamping()
        camping.spendDowntimeHours("actor-1", 2)
        assertEquals(6, camping.downtimeHoursRemaining("actor-1"))
        // Re-rolling the same activity keeps spending: three rolls total = 6h spent, 2h left.
        camping.spendDowntimeHours("actor-1", 2)
        camping.spendDowntimeHours("actor-1", 2)
        assertEquals(2, camping.downtimeHoursRemaining("actor-1"))
    }

    @Test
    fun spendingIsTrackedPerActor() {
        val camping = emptyCamping()
        camping.spendDowntimeHours("actor-1", 2)
        assertEquals(6, camping.downtimeHoursRemaining("actor-1"))
        assertEquals(8, camping.downtimeHoursRemaining("actor-2"))
    }

    @Test
    fun remainingClampsAtZeroAndNeverGoesNegative() {
        val camping = emptyCamping()
        repeat(5) { camping.spendDowntimeHours("actor-1", 2) } // 10h spent
        assertEquals(0, camping.downtimeHoursRemaining("actor-1"))
    }

    @Test
    fun spendAndReadAreConsistentForDottedUuids() {
        // Actor UUIDs contain dots; spend/read must use the same sanitized key.
        val camping = emptyCamping()
        val uuid = "Scene.abc123.Token.def456.Actor.ghi789"
        camping.spendDowntimeHours(uuid, 2)
        camping.spendDowntimeHours(uuid, 2)
        assertEquals(4, camping.downtimeHoursRemaining(uuid))
    }

    @Test
    fun resetZeroesAllSpentHours() {
        val camping = emptyCamping()
        camping.spendDowntimeHours("actor-1", 4)
        camping.spendDowntimeHours("actor-2", 2)
        camping.resetDowntimeHours()
        assertEquals(8, camping.downtimeHoursRemaining("actor-1"))
        assertEquals(8, camping.downtimeHoursRemaining("actor-2"))
    }
}
