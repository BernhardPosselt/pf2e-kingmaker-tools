package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.SetTimeOfDayMode
import at.posselt.pfrpg2e.app.WaitButton
import at.posselt.pfrpg2e.app.forms.TimeInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.app.wait
import at.posselt.pfrpg2e.secondsBetweenNowAndTarget
import at.posselt.pfrpg2e.toUtcInstant
import at.posselt.pfrpg2e.utils.fromDateInputString
import at.posselt.pfrpg2e.utils.getPF2EWorldTime
import at.posselt.pfrpg2e.utils.t
import at.posselt.pfrpg2e.utils.toDateInputString
import at.posselt.pfrpg2e.utils.toLocalUtcDate
import com.foundryvtt.core.Game
import js.objects.recordOf
import kotlinx.browser.localStorage
import kotlinx.coroutines.await
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.js.JsPlainObject
import kotlin.time.Instant

@JsPlainObject
private external interface TimeOfDayData {
    val time: String
}


suspend fun setTimeOfDayMacro(game: Game) {
    val stored = localStorage.getItem("${Config.moduleId}.time-input") ?: "00:00"
    val time = LocalTime.fromDateInputString(stored)
    wait<TimeOfDayData, Unit>(
        title = t("macros.setTimeOfDay.title"),
        templatePath = "components/forms/form.hbs",
        templateContext = recordOf(
            "formRows" to formContext(
                TimeInput(
                    name = "time",
                    label = t("macros.setTimeOfDay.time"),
                    value = time,
                ),
            )
        ),
        buttons = listOf(
            WaitButton(label = t("macros.setTimeOfDay.retract")) { data, action ->
                val now = game.getPF2EWorldTime().toUtcInstant()
                val target = getTargetTime(data.time, now)
                val seconds = secondsBetweenNowAndTarget(now, target, SetTimeOfDayMode.RETRACT)
                advanceTimeTo(game, seconds, target)
            },
            WaitButton(label = t("macros.setTimeOfDay.advance")) { data, action ->
                val now = game.getPF2EWorldTime().toUtcInstant()
                val target = getTargetTime(data.time, now)
                val seconds = secondsBetweenNowAndTarget(now, target, SetTimeOfDayMode.ADVANCE)
                advanceTimeTo(game, seconds, target)
            },
        )
    )
}

private fun getTargetTime(time: String, now: Instant): Instant {
    val targetTime = LocalTime.fromDateInputString(time)
    val targetDate = now.toLocalUtcDate()
    val target = targetDate.atTime(targetTime).toInstant(TimeZone.UTC)
    return target
}

private suspend fun advanceTimeTo(game: Game, seconds: Long, target: Instant) {
    game.time.advance(seconds.toInt()).await()
    val time = target.toLocalDateTime(TimeZone.UTC)
    localStorage.setItem("${Config.moduleId}.time-input", time.time.toDateInputString())
}