package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.camping.getActiveCamping
import at.posselt.pfrpg2e.kingdom.data.RawCharacter
import at.posselt.pfrpg2e.resting.DAY_SECONDS
import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.fromUuidOfTypes
import at.posselt.pfrpg2e.utils.isFirstGM
import at.posselt.pfrpg2e.utils.postChatMessage
import at.posselt.pfrpg2e.utils.t
import at.posselt.pfrpg2e.utils.typeSafeUpdate
import at.posselt.pfrpg2e.weather.rollWeather
import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actor.PF2ECharacter
import com.foundryvtt.pf2e.actor.PF2ENpc
import com.foundryvtt.core.grid.GridHex
import com.foundryvtt.core.grid.GridOffset2D
import com.foundryvtt.core.grid.HexagonalGrid
import com.foundryvtt.core.grid.HexagonalGridCube2D
import com.foundryvtt.core.helpers.TypedHooks
import com.foundryvtt.core.helpers.onUpdateWorldTime
import com.pixijs.Point
import js.objects.recordOf
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.round

/**
 * Registers the daily ticking hooks.
 *
 * Unlike the kingdom turn (monthly, driven by the End Turn button and
 * [TurnTickingEngine]), these are *day-scale* ticks driven automatically by the
 * Foundry world clock. They fire whenever world time crosses one or more calendar
 * day boundaries — which happens when the party rests in the camping sheet, uses
 * the Set Time of Day macro, or the GM advances the clock.
 *
 * On each new day this:
 * - Rolls the daily weather via `rollWeather` (same path as the manual macro:
 *   flat checks, weather events, scene FX, chat), gated by the weather setting and
 *   the camping "Check Weather" toggle.
 * - Moves traveling companions' tokens toward their destination hex and posts an
 *   arrival message when they get there.
 */
fun registerDailyTickHooks(game: Game) {
	TypedHooks.onUpdateWorldTime { worldTime, deltaInSeconds, _, _ ->
		val daysPassed = daysCrossed(worldTime, deltaInSeconds)
		if (game.isFirstGM() && daysPassed >= 1) {
			buildPromise {
				rollDailyWeather(game)
				tickCompanionTravel(game, daysPassed)
			}
		}
	}
}

/**
 * Number of calendar-day boundaries crossed by advancing from
 * (`worldTime` - `deltaInSeconds`) to `worldTime`. Zero or negative when time did
 * not advance past a day boundary (or was rewound).
 */
private fun daysCrossed(worldTime: Int, deltaInSeconds: Int): Int {
	if (deltaInSeconds <= 0) return 0
	val previous = worldTime - deltaInSeconds
	return worldTime.floorDiv(DAY_SECONDS) - previous.floorDiv(DAY_SECONDS)
}

private suspend fun rollDailyWeather(game: Game) {
	val weatherEnabled = game.settings.pfrpg2eKingdomCampingWeather.getEnableWeather()
	// Honor the camping sheet's per-rest "Check Weather" toggle when a camp is active.
	val camping = game.getActiveCamping()
	val skipWeather = camping != null && camping.restSettings.skipWeather
	if (weatherEnabled && !skipWeather) {
		rollWeather(game)
	}
}

private suspend fun tickCompanionTravel(game: Game, daysPassed: Int) {
	game.getKingdomActors().forEach { actor ->
		val kingdom = actor.getKingdom() ?: return@forEach
		val companions = kingdom.companions ?: return@forEach
		var changed = false
		for (companion in companions) {
			// En route = active with a destination set. Movement is gradual: the token
			// steps toward the destination each day rather than teleporting on arrival.
			if (!companion.active) continue
			if (companion.destinationX == null || companion.destinationY == null) continue
			val arrived = advanceCompanionTravel(game, companion, daysPassed)
			changed = true
			companion.traveling = !arrived
			if (arrived) {
				announceArrival(game, companion)
				// Clear the trip so it doesn't keep "arriving" on subsequent days.
				companion.destinationX = null
				companion.destinationY = null
				companion.eta = null
			}
		}
		if (changed) {
			kingdom.companions = companions
			actor.setKingdom(kingdom)
		}
	}
}

/**
 * Advance one companion's token toward its destination hex by [daysPassed] days.
 *
 * Rate:
 *  - ETA set (>= 1): paced so it arrives in exactly that many days.
 *  - ETA blank: `speed` hexes per day.
 *
 * Returns true if the token reached the destination on this tick. Mutates
 * [RawCharacter.eta] as it counts down. Falls back to an ETA-only countdown (so the
 * arrival chat still fires) when there is no token to move on the active hex scene.
 */
private suspend fun advanceCompanionTravel(game: Game, companion: RawCharacter, daysPassed: Int): Boolean {
	val destX = companion.destinationX ?: return false
	val destY = companion.destinationY ?: return false

	val activeScene = game.scenes.active
	val uuid = companion.actorUuid
	val companionActor = uuid?.let { fromUuidOfTypes(it, PF2ECharacter::class, PF2ENpc::class) }
	val tokenDoc = if (activeScene != null && activeScene.grid.isHexagonal && companionActor != null) {
		activeScene.tokens.contents.find { it.actorId == companionActor.id }
	} else {
		null
	}

	if (activeScene == null || tokenDoc == null) {
		// No token to move -> ETA-only countdown so the arrival chat still fires.
		val eta = companion.eta ?: return false
		val newEta = eta - daysPassed
		companion.eta = if (newEta <= 0) null else newEta
		return newEta <= 0
	}

	val grid = activeScene.grid
	val hexGrid = grid.unsafeCast<HexagonalGrid>()
	val centerPoint = Point(
		x = tokenDoc.x + grid.sizeX / 2.0,
		y = tokenDoc.y + grid.sizeY / 2.0,
	)
	val currentOffset = grid.getOffset(centerPoint)
	// Foundry offsets are { i = row (Y), j = column (X) }.
	val destOffset = GridOffset2D(i = destY, j = destX)
	val currentCube = GridHex(currentOffset, hexGrid).cube
	val destCube = GridHex(destOffset, hexGrid).cube
	val distance = cubeDistance(currentCube, destCube)

	if (distance <= 0) {
		companion.eta = null
		return true
	}

	val eta = companion.eta
	val stepHexes: Int
	val arrivedByEta: Boolean
	if (eta != null && eta >= 1) {
		// Pace so the remaining distance is covered in exactly `eta` more days.
		stepHexes = ceil(distance.toDouble() * daysPassed / eta).toInt().coerceAtLeast(1)
		val newEta = eta - daysPassed
		companion.eta = if (newEta <= 0) null else newEta
		arrivedByEta = newEta <= 0
	} else {
		stepHexes = companion.speed.coerceAtLeast(0) * daysPassed
		companion.eta = null
		arrivedByEta = false
		if (stepHexes <= 0) {
			return false
		}
	}

	val arrived = arrivedByEta || stepHexes >= distance
	val targetCube = if (arrived) destCube else cubeLerpStep(currentCube, destCube, stepHexes, distance)
	val point = GridHex(targetCube, hexGrid).topLeft
	tokenDoc.typeSafeUpdate {
		x = point.x
		y = point.y
	}
	return arrived
}

private suspend fun announceArrival(game: Game, companion: RawCharacter) {
	val companionName = companion.actorUuid
		?.let { fromUuidOfTypes(it, PF2ECharacter::class, PF2ENpc::class)?.name }
		?: companion.name
	val destX = companion.destinationX ?: 0
	val destY = companion.destinationY ?: 0
	postChatMessage(
		t("kingdom.companionArrival", recordOf("name" to companionName, "x" to destX, "y" to destY)),
		isHtml = true,
	)
}

/** Hex distance between two cube coordinates. */
private fun cubeDistance(a: HexagonalGridCube2D, b: HexagonalGridCube2D): Int =
	(abs(a.q - b.q) + abs(a.r - b.r) + abs(a.s - b.s)) / 2

/** The cube coordinate [steps] hexes along the straight line from [from] to [to]. */
private fun cubeLerpStep(
	from: HexagonalGridCube2D,
	to: HexagonalGridCube2D,
	steps: Int,
	distance: Int,
): HexagonalGridCube2D {
	if (distance <= 0) return to
	val t = (steps.toDouble() / distance.toDouble()).coerceIn(0.0, 1.0)
	return cubeRound(
		from.q + (to.q - from.q) * t,
		from.r + (to.r - from.r) * t,
		from.s + (to.s - from.s) * t,
	)
}

/** Round fractional cube coordinates to the nearest valid hex (q + r + s == 0). */
private fun cubeRound(q: Double, r: Double, s: Double): HexagonalGridCube2D {
	var rq = round(q)
	var rr = round(r)
	var rs = round(s)
	val dq = abs(rq - q)
	val dr = abs(rr - r)
	val ds = abs(rs - s)
	if (dq > dr && dq > ds) {
		rq = -rr - rs
	} else if (dr > ds) {
		rr = -rq - rs
	} else {
		rs = -rq - rr
	}
	return HexagonalGridCube2D(q = rq.toInt(), r = rr.toInt(), s = rs.toInt())
}
