package at.posselt.pfrpg2e

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createInstant(
    year: Int = 2008,
    month: Int = 1,
    day: Int = 1,
    hour: Int = 0,
    minute: Int = 0,
    second: Int = 0,
) = LocalDateTime(year, month, day, hour, minute, second, 0).toUtcInstant()

class TimeKtTest {

    @Test
    fun testSecondsAdvance() {
        assertEquals(
            1, secondsBetweenNowAndTarget(
                now = createInstant(second = 1),
                target = createInstant(second = 2),
                SetTimeOfDayMode.ADVANCE,
            )
        )
    }

    @Test
    fun testSecondsAdvanceBefore() {
        assertEquals(
            (3600 * 24) - 1, secondsBetweenNowAndTarget(
                now = createInstant(second = 2),
                target = createInstant(second = 1),
                SetTimeOfDayMode.ADVANCE,
            )
        )
    }

    @Test
    fun testSecondsRetract() {
        assertEquals(
            -1, secondsBetweenNowAndTarget(
                now = createInstant(second = 2),
                target = createInstant(second = 1),
                SetTimeOfDayMode.RETRACT,
            )
        )
    }

    @Test
    fun testSecondsRetractAfter() {
        assertEquals(
            (-3600 * 24) + 1, secondsBetweenNowAndTarget(
                now = createInstant(second = 1),
                target = createInstant(second = 2),
                SetTimeOfDayMode.RETRACT,
            )
        )
    }

}