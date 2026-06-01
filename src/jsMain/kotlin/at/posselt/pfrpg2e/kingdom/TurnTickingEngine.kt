package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.kingdom.structures.CommodityStorage
import at.posselt.pfrpg2e.data.regions.WeatherEffect
import at.posselt.pfrpg2e.data.regions.WeatherType
import at.posselt.pfrpg2e.data.regions.findWeatherType
import at.posselt.pfrpg2e.kingdom.data.RawCommodities
import at.posselt.pfrpg2e.kingdom.data.RawCurrentCommodities
import at.posselt.pfrpg2e.kingdom.RawModifier
import at.posselt.pfrpg2e.kingdom.data.endTurn
import at.posselt.pfrpg2e.kingdom.data.limitBy
import at.posselt.pfrpg2e.kingdom.data.RawConsumption
import at.posselt.pfrpg2e.kingdom.RawCouncilCooldowns
import at.posselt.pfrpg2e.kingdom.data.RawFame
import at.posselt.pfrpg2e.kingdom.data.RawResources
import kotlinx.js.JsPlainObject

/**
 * Represents the diff of a single tick operation for auditing/logging.
 */
data class TickChange(
	val category: String,
	val field: String,
	val oldValue: Any?,
	val newValue: Any?,
)

/**
 * Represents a timed injury that heals over a number of kingdom turns.
 *
 * Duration convention (same as modifier turns):
 * - null or 0 = permanent (never heals on its own)
 * - 1 = expires after this tick (remove the injury)
 * - >1 = decrement by 1 each tick
 */
@JsPlainObject
external interface Injury {
	var id: String
	var name: String
	var duration: Int?
}

/**
 * Engine output: all state changes produced by a turn tick.
 * Each field holds the post-tick value to apply to KingdomData.
 */
data class TickResult(
	val supernaturalSolutions: Int,
	val creativeSolutions: Int,
	val fame: RawFame,
	val resourcePoints: RawResources,
	val resourceDice: RawResources,
	val consumption: RawConsumption,
	val commodities: RawCurrentCommodities,
	val councilCooldowns: RawCouncilCooldowns?,
	val modifiers: Array<RawModifier>,
	val injuries: Array<Injury>,
	val changes: List<TickChange>,
	val weatherType: WeatherType? = null,
	val weatherEffect: WeatherEffect? = null,
)

/**
 * Input parameters for pure weather-shift resolution.
 * All rolls are passed in from the caller so the engine stays deterministic.
 */
data class WeatherShiftParams(
	val coldDc: Int?,
	val precipitationDc: Int?,
	val weatherEventDc: Int?,
	/** Pre-rolled d20 value for the cold check; null if no DC configured */
	val coldRoll: Int?,
	/** Pre-rolled d20 value for the precipitation check; null if no DC configured */
	val precipitationRoll: Int?,
	/** Pre-rolled d20 value for the weather event check; null if no DC configured */
	val weatherEventRoll: Int?,
)

/**
 * Turn-triggered ticking engine.
 *
 * Processes all time-based state transitions that occur at the end of a kingdom turn:
 * - Resets one-shot solution counters
 * - Advances fame, resource points, resource dice, consumption
 * - Merges commodities with storage limits
 * - Counts down council cooldowns
 * - Ticks down modifier durations and expires finished modifiers
 * - Ticks down injury durations and removes expired injuries
 * - Resolves weather shift from pre-rolled climate checks
 *
 * The engine is designed to be called from Foundry-side code (KingdomSheet) but
 * contains no Foundry/Game dependencies, making it fully unit-testable.
 */
object TurnTickingEngine {

	/**
	 * Run a single turn [tick] against the provided kingdom state snapshots.
	 *
	 * @param fame Current fame state (now/next).
	 * @param resourcePoints Current resource point state.
	 * @param resourceDice Current resource dice state.
	 * @param consumption Current consumption state.
	 * @param commodities Current commodity state.
	 * @param storage Commodity storage capacity (used to cap end-turn merge).
	 * @param councilCooldowns Nullable council cooldown state.
	 * @param modifiers Current array of active modifiers (may carry turn durations).
	 * @param injuries Current array of active injuries (may carry turn durations).
	 * @param weatherShift Optional weather shift parameters; when provided, weather is advanced.
	 * @return [TickResult] with all post-tick values and a list of changes.
	 */
	fun tick(
		fame: RawFame,
		resourcePoints: RawResources,
		resourceDice: RawResources,
		consumption: RawConsumption,
		commodities: RawCurrentCommodities,
		storage: CommodityStorage,
		councilCooldowns: RawCouncilCooldowns?,
		modifiers: Array<RawModifier>,
		injuries: Array<Injury> = emptyArray(),
		weatherShift: WeatherShiftParams? = null,
	): TickResult {
		val changes = mutableListOf<TickChange>()

		// 1) Reset solution counters
		changes += TickChange("solutions", "supernaturalSolutions", null, 0)
		changes += TickChange("solutions", "creativeSolutions", null, 0)

		// 2) Advance fame: next -> now, next reset to 0
		val newFame = RawFame(
			now = fame.next,
			next = 0,
			type = fame.type,
		)
		if (newFame.now != fame.now) {
			changes += TickChange("fame", "now", fame.now, newFame.now)
		}
		if (newFame.next != fame.next) {
			changes += TickChange("fame", "next", fame.next, newFame.next)
		}

		// 3) Advance resource points: next -> now
		val newResourcePoints = resourcePoints.endTurn()
		if (newResourcePoints.now != resourcePoints.now) {
			changes += TickChange("resourcePoints", "now", resourcePoints.now, newResourcePoints.now)
		}
		if (newResourcePoints.next != resourcePoints.next) {
			changes += TickChange("resourcePoints", "next", resourcePoints.next, newResourcePoints.next)
		}

		// 4) Advance resource dice: next -> now
		val newResourceDice = resourceDice.endTurn()
		if (newResourceDice.now != resourceDice.now) {
			changes += TickChange("resourceDice", "now", resourceDice.now, newResourceDice.now)
		}
		if (newResourceDice.next != resourceDice.next) {
			changes += TickChange("resourceDice", "next", resourceDice.next, newResourceDice.next)
		}

		// 5) Advance consumption: next -> now
		val newConsumption = consumption.endTurn()
		if (newConsumption.now != consumption.now) {
			changes += TickChange("consumption", "now", consumption.now, newConsumption.now)
		}

		// 6) Merge commodities with storage cap
		val newCommodities = commodities.endTurn(storage)

		// 7) Tick down council cooldowns
		val newCooldowns = if (councilCooldowns != null) {
			val updated = RawCouncilCooldowns(
				audit = (councilCooldowns.audit - 1).coerceAtLeast(0),
				scrying = (councilCooldowns.scrying - 1).coerceAtLeast(0),
				lockdown = (councilCooldowns.lockdown - 1).coerceAtLeast(0),
				feast = (councilCooldowns.feast - 1).coerceAtLeast(0),
			)
			if (updated.audit != councilCooldowns.audit) {
				changes += TickChange("councilCooldowns", "audit", councilCooldowns.audit, updated.audit)
			}
			if (updated.scrying != councilCooldowns.scrying) {
				changes += TickChange("councilCooldowns", "scrying", councilCooldowns.scrying, updated.scrying)
			}
			if (updated.lockdown != councilCooldowns.lockdown) {
				changes += TickChange("councilCooldowns", "lockdown", councilCooldowns.lockdown, updated.lockdown)
			}
			if (updated.feast != councilCooldowns.feast) {
				changes += TickChange("councilCooldowns", "feast", councilCooldowns.feast, updated.feast)
			}
			updated
		} else {
			null
		}

		// 8) Tick down modifier durations
		var expiredCount = 0
		val newModifiers = modifiers.mapNotNull { mod ->
			val turns = mod.turns
			if (turns == null || turns == 0) {
				// Permanent modifier, keep as-is
				mod
			} else if (turns <= 1) {
				// Expired after this tick
				expiredCount++
				null
			} else {
				// Decrement remaining turns
				RawModifier(
					id = mod.id,
					type = mod.type,
					value = mod.value,
					name = mod.name,
					enabled = mod.enabled,
					turns = turns - 1,
					buttonLabel = mod.buttonLabel,
					valueExpression = mod.valueExpression,
					isConsumedAfterRoll = mod.isConsumedAfterRoll,
					rollOptions = mod.rollOptions,
					applyIf = mod.applyIf,
					fortune = mod.fortune,
					rollTwiceKeepLowest = mod.rollTwiceKeepLowest,
					rollTwiceKeepHighest = mod.rollTwiceKeepHighest,
					upgradeResults = mod.upgradeResults,
					downgradeResults = mod.downgradeResults,
					notes = mod.notes,
					requiresTranslation = mod.requiresTranslation,
					selector = mod.selector,
				)
			}
		}.toTypedArray()

		if (expiredCount > 0) {
			changes += TickChange("modifiers", "expired", null, expiredCount)
		}

		// 9) Tick down injury durations
		val (newInjuries, injuryChanges) = tickInjuryDurations(injuries)
		changes += injuryChanges

		// 10) Resolve weather shift
		val (newWeatherType, newWeatherEffect) = if (weatherShift != null) {
			resolveWeatherShift(weatherShift, changes)
		} else {
			null to null
		}

		return TickResult(
			supernaturalSolutions = 0,
			creativeSolutions = 0,
			fame = newFame,
			resourcePoints = newResourcePoints,
			resourceDice = newResourceDice,
			consumption = newConsumption,
			commodities = newCommodities,
			councilCooldowns = newCooldowns,
			modifiers = newModifiers,
			injuries = newInjuries,
			changes = changes,
			weatherType = newWeatherType,
			weatherEffect = newWeatherEffect,
		)
	}

	/**
	 * Pure function that resolves a weather shift from pre-rolled climate checks.
	 *
	 * Takes pre-rolled d20 values so the engine remains deterministic and testable.
	 * The caller (KingdomSheet) is responsible for rolling the dice via Foundry's
	 * roll API and passing the results in.
	 *
	 * @param params Pre-rolled weather shift parameters.
	 * @param changes Mutable list of [TickChange] records to append weather changes to.
	 * @return Pair of (new WeatherType, new WeatherEffect).
	 */
	fun resolveWeatherShift(
		params: WeatherShiftParams,
		changes: MutableList<TickChange>,
	): Pair<WeatherType, WeatherEffect> {
		val isCold = params.coldDc != null && params.coldRoll != null && params.coldRoll >= params.coldDc
		val hasPrecipitation = params.precipitationDc != null && params.precipitationRoll != null && params.precipitationRoll >= params.precipitationDc
		val type = findWeatherType(isCold = isCold, hasPrecipitation = hasPrecipitation)
		val effect = when (type) {
			WeatherType.COLD -> WeatherEffect.SUNNY
			WeatherType.SNOWY -> WeatherEffect.SNOW
			WeatherType.RAINY -> WeatherEffect.RAIN
			WeatherType.SUNNY -> WeatherEffect.SUNNY
		}
		changes += TickChange("weather", "type", null, type)
		changes += TickChange("weather", "effect", null, effect)
		return type to effect
	}

	/**
	 * Pure function that decrements injury durations by one tick.
	 *
	 * Duration convention (identical to modifier turns):
	 * - null or 0 = permanent injury, never heals on its own
	 * - 1 = expires after this tick (removed from the result)
	 * - >1 = decrement duration by 1
	 *
	 * @param injuries Current array of active injuries.
	 * @return Pair of (surviving injuries after tick, list of TickChange records).
	 */
	fun tickInjuryDurations(injuries: Array<Injury>): Pair<Array<Injury>, List<TickChange>> {
		val changes = mutableListOf<TickChange>()
		var expiredCount = 0
		val remaining = injuries.mapNotNull { injury ->
			val duration = injury.duration
			if (duration == null || duration == 0) {
				// Permanent injury, keep as-is
				injury
			} else if (duration <= 1) {
				// Expired after this tick
				expiredCount++
				changes += TickChange("injuries", "expired", injury.id, injury.name)
				null
			} else {
				// Decrement remaining duration
				changes += TickChange(
					"injuries",
					"duration",
					duration,
					duration - 1,
				)
				Injury(
					id = injury.id,
					name = injury.name,
					duration = duration - 1,
				)
			}
		}.toTypedArray()

		if (expiredCount > 1) {
			changes += TickChange("injuries", "expiredCount", null, expiredCount)
		}

		return Pair(remaining, changes)
	}
}
