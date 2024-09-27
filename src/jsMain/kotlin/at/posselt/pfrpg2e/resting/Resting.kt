package at.posselt.pfrpg2e.resting

import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.actions.ActionMessage
import at.posselt.pfrpg2e.actions.handlers.GainProvisions
import at.posselt.pfrpg2e.actor.party
import at.posselt.pfrpg2e.camping.ActivityEffect
import at.posselt.pfrpg2e.camping.CampingData
import at.posselt.pfrpg2e.camping.MealEffect
import at.posselt.pfrpg2e.camping.RecipeData
import at.posselt.pfrpg2e.camping.applyRestHealEffects
import at.posselt.pfrpg2e.camping.askDc
import at.posselt.pfrpg2e.camping.calculateDailyPreparationSeconds
import at.posselt.pfrpg2e.camping.calculateRestDurationSeconds
import at.posselt.pfrpg2e.camping.campingActivitiesDoublingHealing
import at.posselt.pfrpg2e.camping.dialogs.RestRollMode
import at.posselt.pfrpg2e.camping.dialogs.play
import at.posselt.pfrpg2e.camping.getActorsInCamp
import at.posselt.pfrpg2e.camping.getAllActivities
import at.posselt.pfrpg2e.camping.getAllRecipes
import at.posselt.pfrpg2e.camping.getAppliedCampingEffects
import at.posselt.pfrpg2e.camping.getAppliedMealEffects
import at.posselt.pfrpg2e.camping.getCampingActorsByUuid
import at.posselt.pfrpg2e.camping.getMealEffectItems
import at.posselt.pfrpg2e.camping.mealEffectsChangingRestDuration
import at.posselt.pfrpg2e.camping.mealEffectsDoublingHealing
import at.posselt.pfrpg2e.camping.mealEffectsHalvingHealing
import at.posselt.pfrpg2e.camping.performCampingCheck
import at.posselt.pfrpg2e.camping.removeCombatEffects
import at.posselt.pfrpg2e.camping.removeMealEffects
import at.posselt.pfrpg2e.camping.removeProvisions
import at.posselt.pfrpg2e.camping.rollRandomEncounter
import at.posselt.pfrpg2e.camping.setCamping
import at.posselt.pfrpg2e.data.actor.Perception
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.utils.awaitAll
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.formatSeconds
import at.posselt.pfrpg2e.utils.postChatMessage
import at.posselt.pfrpg2e.utils.typeSafeUpdate
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actions.RestForTheNightOptions
import com.foundryvtt.pf2e.actor.PF2EActor
import com.foundryvtt.pf2e.actor.PF2ECharacter
import com.foundryvtt.pf2e.actor.PF2ENpc
import com.foundryvtt.pf2e.pf2e
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

private const val EIGHT_HOURS_SECONDS = 8 * 60 * 60
private const val FOUR_HOURS_SECONDS = 4 * 3600

private suspend fun getRestSecondsPerPlayer(
    players: List<PF2EActor>,
    recipes: List<RecipeData>,
    increaseActorsKeepingWatch: Int = 0,
): List<Int> {
    val mealEffects = mealEffectsChangingRestDuration(recipes)
    val durationPerPlayer = players.asSequence()
        .map {
            buildPromise {
                EIGHT_HOURS_SECONDS + it.getAppliedMealEffects(mealEffects)
                    .mapNotNull(MealEffect::changeRestDurationSeconds)
                    .sum()
            }
        }
        .toList()
        .awaitAll()
    val additionalWatchers = generateSequence { EIGHT_HOURS_SECONDS }
        .take(increaseActorsKeepingWatch)
        .toList()
    return durationPerPlayer + additionalWatchers
}

private suspend fun getFullRestSeconds(
    watchers: List<PF2EActor>,
    recipes: List<RecipeData>,
    gunsToClean: Int,
    increaseActorsKeepingWatch: Int,
): Int = calculateRestDurationSeconds(getRestSecondsPerPlayer(watchers, recipes, increaseActorsKeepingWatch)) +
        calculateDailyPreparationSeconds(gunsToClean)

data class RestDuration(
    val value: Int,
) {
    val label: String
        get() = formatSeconds(value)
}


data class TotalRestDuration(
    val total: RestDuration,
    val left: RestDuration?,
)

suspend fun getTotalRestDuration(
    watchers: List<PF2EActor>,
    recipes: List<RecipeData>,
    gunsToClean: Int,
    increaseActorsKeepingWatch: Int = 0,
    remainingSeconds: Int? = null,
): TotalRestDuration {
    val total = getFullRestSeconds(
        watchers = watchers,
        recipes = recipes,
        gunsToClean = gunsToClean,
        increaseActorsKeepingWatch = increaseActorsKeepingWatch
    )
    return TotalRestDuration(
        total = RestDuration(total),
        left = remainingSeconds
            ?.takeIf { it > 0 }
            ?.let(::RestDuration)
    )
}

private enum class HealMultiplier {
    HALVE,
    DOUBLE,
}

private fun PF2ECharacter.additionalHealingAfterRest(multiplier: HealMultiplier?): Int {
    return if (multiplier != null) {
        val healed = max(
            1,
            system.abilities.con.mod
        ) * system.details.level.value * hitPoints.recoveryMultiplier + hitPoints.recoveryAddend
        if (multiplier == HealMultiplier.DOUBLE) {
            healed
        } else {
            val healedWithHalf = (healed / 2) + system.attributes.hp.value
            if (healedWithHalf >= system.attributes.hp.max) {
                0
            } else {
                -(healed / 2)
            }
        }
    } else {
        0
    }
}

private suspend fun findHealMultiplier(
    actor: PF2ECharacter,
    recipesDoublingHealing: List<MealEffect>,
    recipesHalvingHealing: List<MealEffect>,
    activitiesDoublingHealing: List<ActivityEffect>,
): HealMultiplier? {
    val doubles = actor.getAppliedMealEffects(recipesDoublingHealing).isNotEmpty()
            || actor.getAppliedCampingEffects(activitiesDoublingHealing).isNotEmpty()
    val halves = actor.getAppliedMealEffects(recipesHalvingHealing).isNotEmpty()
    return if (doubles && halves) {
        null
    } else if (doubles) {
        HealMultiplier.DOUBLE
    } else if (halves) {
        HealMultiplier.HALVE
    } else {
        null
    }
}

private suspend fun additionalHealingPerActorAfterRest(
    recipes: List<RecipeData>,
    camping: CampingData,
    actors: List<PF2EActor>
): List<Pair<PF2ECharacter, Int>> = coroutineScope {
    val recipesDoublingHealing = mealEffectsDoublingHealing(recipes)
    val recipesHalvingHealing = mealEffectsHalvingHealing(recipes)
    val activitiesDoublingHealing = campingActivitiesDoublingHealing(camping.getAllActivities().toList())
    actors
        .filterIsInstance<PF2ECharacter>()
        .map { actor ->
            async {
                val multiplier = findHealMultiplier(
                    actor,
                    recipesDoublingHealing = recipesDoublingHealing,
                    recipesHalvingHealing = recipesHalvingHealing,
                    activitiesDoublingHealing = activitiesDoublingHealing
                )
                actor to actor.additionalHealingAfterRest(multiplier)
            }
        }
        .awaitAll()
}

private suspend fun applyAdditionalHealing(healingAfterRest: List<Pair<PF2ECharacter, Int>>) = coroutineScope {
    healingAfterRest.map { (actor, healing) ->
        val value = min(actor.hitPoints.value + healing, actor.hitPoints.max)
        async {
            if (healing > 0) {
                postChatMessage("Healing $healing HP more from recipes or camping activities", speaker = actor)
            } else if (healing < 0) {
                postChatMessage("Healing ${abs(healing)}healing HP fewer from recipes", speaker = actor)
            }
            actor.typeSafeUpdate { system.attributes.hp.value = value }
        }
    }.awaitAll()
}

private suspend fun findRandomEncounterAt(
    game: Game,
    campingActor: PF2ENpc,
    camping: CampingData,
    watchDurationSeconds: Int,
): Int? {
    val randomEncounterChecksAtSeconds = when (fromCamelCase<RestRollMode>(camping.restRollMode)) {
        RestRollMode.ONE -> List(1) { Random.nextInt(1, watchDurationSeconds - 1) }
        RestRollMode.ONE_EVERY_FOUR_HOURS -> List(watchDurationSeconds / (FOUR_HOURS_SECONDS)) { index ->
            val begin = index * FOUR_HOURS_SECONDS
            val end = index * FOUR_HOURS_SECONDS + FOUR_HOURS_SECONDS
            Random.nextInt(begin + 1, end - 1)
        }

        else -> emptyList()
    }
    for (checksAtSecond in randomEncounterChecksAtSeconds) {
        if (rollRandomEncounter(game = game, actor = campingActor, includeFlatCheck = true)) {
            return checksAtSecond
        }
    }
    return null
}

private suspend fun beginRest(
    game: Game,
    dispatcher: ActionDispatcher,
    campingActor: PF2ENpc,
    camping: CampingData,
) {
    val actorsByUuid = getCampingActorsByUuid(camping.actorUuids).associateBy(PF2EActor::uuid)
    val watchers = actorsByUuid.values
        .filter { !camping.actorUuidsNotKeepingWatch.contains(it.uuid) }
    val watchDurationSeconds = getFullRestSeconds(
        watchers = watchers,
        recipes = camping.getAllRecipes().toList(),
        gunsToClean = camping.gunsToClean,
        increaseActorsKeepingWatch = camping.increaseWatchActorNumber,
    )
    val randomEncounterAt = findRandomEncounterAt(
        game = game,
        campingActor = campingActor,
        camping = camping,
        watchDurationSeconds = watchDurationSeconds,
    )
    if (randomEncounterAt != null) {
        watchers
            .filterIsInstance<PF2ECharacter>()
            .randomOrNull()
            ?.performCampingCheck(
                isSecret = true,
                isWatch = true,
                attribute = Perception,
                dc = askDc("Enemy Stealth"),
            )
        game.time.advance(randomEncounterAt).await()
        camping.watchSecondsRemaining = randomEncounterAt
        campingActor.setCamping(camping)
    } else {
        camping.watchSecondsRemaining = watchDurationSeconds
        completeDailyPreparations(game, dispatcher, campingActor, camping)
    }
}

private suspend fun completeDailyPreparations(
    game: Game,
    dispatcher: ActionDispatcher,
    campingActor: PF2ENpc,
    camping: CampingData,
) =
    coroutineScope {
        val actors = camping.getActorsInCamp()
        val recipes = camping.getAllRecipes().toList()

        game.time.advance(camping.watchSecondsRemaining).await()
        camping.watchSecondsRemaining = 0
        camping.encounterModifier = 0
        camping.dailyPrepsAtTime = game.time.worldTime
        camping.campingActivities.forEach { it.result = null }
        camping.cooking.results.forEach { it.result = null }
        campingActor.setCamping(camping)

        val additionalHealing = additionalHealingPerActorAfterRest(recipes, camping, actors)
        game.pf2e.actions.restForTheNight(RestForTheNightOptions(actors = actors.toTypedArray(), skipDialog = true))
            .await()
        applyAdditionalHealing(additionalHealing)
        applyRestHealEffects(actors, recipes, getMealEffectItems(recipes))
        removeMealEffects(recipes, actors, onlyRemoveAfterRest = true)
        removeProvisions(actors + listOfNotNull(game.party()))
        removeCombatEffects(actors)
        gainMinimumSubsistence(game, dispatcher, camping.cooking.minimumSubsistence)
    }

private suspend fun gainMinimumSubsistence(
    game: Game,
    dispatcher: ActionDispatcher,
    quantity: Int,
) {
    game.party()?.let {
        dispatcher.dispatch(
            ActionMessage(
                action = "gainProvisions",
                data = GainProvisions(
                    actorUuid = it.uuid,
                    quantity = quantity,
                ).unsafeCast<AnyObject>()
            )
        )
    }
}


suspend fun rest(
    game: Game,
    dispatcher: ActionDispatcher,
    campingActor: PF2ENpc,
    camping: CampingData
) {
    if (camping.watchSecondsRemaining == 0) {
        camping.restingTrack?.play()
        beginRest(game, dispatcher, campingActor, camping)
    } else {
        completeDailyPreparations(game, dispatcher, campingActor, camping)
    }
}