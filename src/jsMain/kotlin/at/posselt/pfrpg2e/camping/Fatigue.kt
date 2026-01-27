package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.resting.DAY_SECONDS
import at.posselt.pfrpg2e.resting.SIXTEEN_HOURS_SECONDS
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.isFirstGM
import at.posselt.pfrpg2e.utils.worldTimeSeconds
import at.posselt.pfrpg2e.weather.getCurrentWeatherType
import com.foundryvtt.core.Game
import com.foundryvtt.core.helpers.TypedHooks
import com.foundryvtt.core.helpers.onUpdateWorldTime
import com.foundryvtt.pf2e.actor.PF2EActor
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

private suspend fun PF2EActor.getFatigueDurationSeconds(
    recipes: List<RecipeData>,
): Int {
    val relevantEffects = mealEffectsChangingFatigueDuration(recipes)
    val increasedDuration = getAppliedMealEffects(relevantEffects)
        .sumOf { it.changeFatigueDurationSeconds ?: 0 }
    return SIXTEEN_HOURS_SECONDS + increasedDuration
}

suspend fun persistPassedTime(game: Game, deltaInSeconds: Int) {
    // reset if more than one day is passed
    game.getActiveCampingActor()?.let {
        val camping = it.getCamping()!!
        if (deltaInSeconds >= DAY_SECONDS && camping.resetTimeTrackingAfterOneDay) {
            camping.resetTimeTracking(game)
        } else {
            camping.persistPassedTime(deltaInSeconds)
        }
        it.setCamping(camping)
    }
}

fun registerFatiguedHooks(game: Game) {
    TypedHooks.onUpdateWorldTime { _, deltaInSeconds, _, _ ->
        if (game.isFirstGM()) {
            buildPromise {
                persistPassedTime(game, deltaInSeconds)
                val camping = game.getActiveCamping()
                if (camping != null && camping.autoApplyFatigued) {
                    val currentWeatherType = game.getCurrentWeatherType()
                    val fatiguedAfterTravellingSeconds = currentWeatherType.fatigueDurationMultiplier * 8 * 3600
                    val recipes = camping.getAllRecipes().toList()
                    val elapsedSeconds = game.time.worldTimeSeconds - camping.dailyPrepsAtTime
                    val travelSeconds = camping.secondsSpentTraveling
                    buildPromise {
                        val actors = camping.getActorsInCamp()
                        actors
                            .map {
                                async {
                                    val travelledTooMuch = travelSeconds > fatiguedAfterTravellingSeconds
                                    if (travelledTooMuch || elapsedSeconds > it.getFatigueDurationSeconds(
                                            recipes = recipes,
                                        )
                                    ) {
                                        it.increaseCondition("fatigued")
                                    }
                                }
                            }
                            .awaitAll()
                    }
                }
            }
        }
    }
}
