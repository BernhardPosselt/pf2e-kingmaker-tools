package at.posselt.pfrpg2e.camping

import kotlin.test.Test
import kotlin.test.assertEquals

class RestingTest {

    fun testZeroConMod() {
        assertEquals(
            2, calculateRestoredHp(
                currentHp = 1,
                conModifier = 0,
                maxHp = 3,
                level = 2,
            )
        )
    }

    fun testMaxHeal() {
        assertEquals(
            2, calculateRestoredHp(
                currentHp = 1,
                conModifier = 1,
                maxHp = 3,
                level = 3,
            )
        )
    }


    @Test
    fun testAverageRestDuration() {
        val eightHours = 8 * 60 * 60
        val twelveHours = 12 * 60 * 60
        assertEquals(eightHours, calculateRestDurationSeconds(listOf(eightHours)))
        assertEquals(2 * eightHours, calculateRestDurationSeconds(listOf(eightHours, eightHours)))
        assertEquals(twelveHours, calculateRestDurationSeconds(listOf(eightHours, eightHours, eightHours)))
    }
}