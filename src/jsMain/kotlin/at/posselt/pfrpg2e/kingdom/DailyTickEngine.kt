package at.posselt.pfrpg2e.kingdom

/**
 * Result of advancing a single companion's travel by some number of days.
 *
 * @property newEta Remaining ETA in days after the tick (null once arrived / not traveling).
 * @property traveling Whether the companion is still en route after the tick.
 * @property arrived True only on the tick where the companion reaches its destination.
 */
data class TravelTickResult(
	val newEta: Int?,
	val traveling: Boolean,
	val arrived: Boolean,
)

/**
 * Daily ticking engine.
 *
 * Processes the *day-scale* state transitions that the [TurnTickingEngine] (monthly,
 * kingdom-turn scale) deliberately does not own. Right now that is companion travel:
 * an ETA measured in days that counts down toward arrival.
 *
 * Weather is day-scale too, but it is resolved by the existing `rollWeather(game)`
 * function (flat checks, weather events, scene FX, chat) rather than re-implemented
 * here — see `registerDailyTickHooks`.
 *
 * Functions are pure so they can be unit-tested without Foundry. The hook layer
 * sources data and applies side effects (token moves, chat, persistence).
 */
object DailyTickEngine {

	/**
	 * Advance a single companion's travel ETA by [days] elapsed days.
	 *
	 * @param eta Current remaining ETA in days (null = no active ETA).
	 * @param days Number of days elapsed since the last tick (coerced to >= 1).
	 * @return [TravelTickResult] describing the post-tick ETA and whether arrival happened.
	 */
	fun tickTravelEta(eta: Int?, days: Int = 1): TravelTickResult {
		val elapsed = days.coerceAtLeast(1)
		if (eta == null) {
			return TravelTickResult(newEta = null, traveling = false, arrived = false)
		}
		val next = eta - elapsed
		return if (next <= 0) {
			TravelTickResult(newEta = null, traveling = false, arrived = true)
		} else {
			TravelTickResult(newEta = next, traveling = true, arrived = false)
		}
	}
}
