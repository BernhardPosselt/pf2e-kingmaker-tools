package at.posselt.pfrpg2e.utils

import at.posselt.pfrpg2e.data.regions.Month
import at.posselt.pfrpg2e.data.regions.getMonth
import at.posselt.pfrpg2e.toUtcInstant
import com.foundryvtt.core.Game
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlin.js.Date
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

fun Game.getPF2EWorldTime(): LocalDateTime {
    val createdOn = settings.get<String>("pf2e", "worldClock.worldCreatedOn")
    return Instant.parse(createdOn)
        .plus(time.worldTime, DateTimeUnit.SECOND)
        .toLocalDateTime(TimeZone.UTC)
}

fun Game.getCurrentMonth(): Month =
    getMonth(getPF2EWorldTime().monthNumber - 1)

fun Date.toInstant() =
    Instant.fromEpochSeconds(getSeconds().toLong())

fun Date.toLocalUtcDateTime() =
    toInstant().toLocalDateTime(TimeZone.UTC)

fun LocalDateTime.toJsUtcDate() =
    Date(toUtcInstant().epochSeconds)

fun LocalTime.Companion.fromDateInputString(value: String): LocalTime =
    parse(value, Format {
        hour(padding = Padding.ZERO)
        char(':')
        minute(padding = Padding.ZERO)
    })

fun Instant.toLocalUtcDate(): LocalDate =
    toLocalDateTime(TimeZone.UTC).date

fun LocalTime.toDateInputString(): String =
    format(LocalTime.Format {
        hour(padding = Padding.ZERO)
        char(':')
        minute(padding = Padding.ZERO)
    })

fun LocalTime.isDay(): Boolean =
    hour >= 6 && hour < 18

fun formatSeconds(seconds: Int, isNegative: Boolean = false): String {
    val hours = seconds / 3600
    val minutes = (seconds - hours * 3600) / 60
    val time = "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
    return if (isNegative) {
        "-$time"
    } else {
        time
    }
}