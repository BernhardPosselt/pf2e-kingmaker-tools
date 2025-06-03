package at.posselt.pfrpg2e.utils

import at.posselt.pfrpg2e.data.regions.Month
import at.posselt.pfrpg2e.data.regions.getMonth
import at.posselt.pfrpg2e.toUtcInstant
import com.foundryvtt.core.Game
import com.foundryvtt.core.helpers.GameTime
import com.foundryvtt.pf2e.pf2e
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.js.Date

fun Game.getPF2EWorldTime(): LocalDateTime {
    val ms = pf2e.worldClock.worldTime.valueOf()
    return Instant.fromEpochMilliseconds(ms.toLong())
        .toLocalDateTime(TimeZone.UTC)
}

fun Game.getCurrentMonth(): Month =
    getMonth(getPF2EWorldTime().monthNumber - 1)

fun Date.toInstant() =
    Instant.fromEpochSeconds(getSeconds().toLong())

@Suppress("unused")
fun Date.toLocalUtcDateTime() =
    toInstant().toLocalDateTime(TimeZone.UTC)

@Suppress("unused")
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

val GameTime.worldTimeSeconds: Int
    get() = worldTime.toInt()