package at.posselt.pfrpg2e

import kotlinx.datetime.*

fun LocalDateTime.toUtcInstant() =
    toInstant(TimeZone.UTC)

enum class SetTimeOfDayMode {
    ADVANCE,
    RETRACT
}

private fun daysToAdd(now: Instant, target: Instant, mode: SetTimeOfDayMode): Int =
    if (now >= target && mode == SetTimeOfDayMode.ADVANCE) {
        1
    } else if (now <= target && mode == SetTimeOfDayMode.RETRACT) {
        -1
    } else {
        0
    }

fun secondsBetweenNowAndTarget(now: Instant, target: Instant, mode: SetTimeOfDayMode): Long {
    val addDays = daysToAdd(now, target, mode)
    val targetDay = target.plus(addDays, DateTimeUnit.DAY, TimeZone.UTC)
    return targetDay.epochSeconds - now.epochSeconds
}

fun calculateHexplorationActivities(speedInFeat: Int): Double =
    when {
        speedInFeat <= 10 -> .5
        speedInFeat < 30 -> 1.0
        speedInFeat < 45 -> 2.0
        speedInFeat < 60 -> 3.0
        else -> 4.0
    }