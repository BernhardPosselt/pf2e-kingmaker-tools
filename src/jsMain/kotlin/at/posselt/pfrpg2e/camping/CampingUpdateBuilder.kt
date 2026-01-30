package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.camping.dialogs.RegionSettings
import at.posselt.pfrpg2e.camping.dialogs.Track
import at.posselt.pfrpg2e.utils.DocumentUpdateDsl
import at.posselt.pfrpg2e.utils.PropertyUpdateBuilder
import at.posselt.pfrpg2e.utils.RecordPropertyUpdateBuilder
import js.objects.Record
import js.objects.unsafeJso

@DocumentUpdateDsl
@Suppress("unused")
class CookingResultPropertyBuilder(basePath: String, updates: Record<String, Any?>, propertyName: String) :
    RecordPropertyUpdateBuilder<Record<String, CookingResult>>(basePath, updates, propertyName) {
    val result = PropertyUpdateBuilder<String?>(propertyName, updates, "result")
    val skill = PropertyUpdateBuilder<String>(propertyName, updates, "skill")

    operator fun invoke(action: CookingResultPropertyBuilder.() -> Unit) = action()
}

@Suppress("unused")
class ActorMealPropertyBuilder(basePath: String, updates: Record<String, Any?>, propertyName: String) :
    RecordPropertyUpdateBuilder<Record<String, ActorMeal>>(basePath, updates, propertyName) {
    val actorUuid = PropertyUpdateBuilder<String>(propertyName, updates, "actorUuid")
    val favoriteMeal = PropertyUpdateBuilder<String?>(propertyName, updates, "favoriteMeal")
    val chosenMeal = PropertyUpdateBuilder<String>(propertyName, updates, "chosenMeal")

    operator fun invoke(action: ActorMealPropertyBuilder.() -> Unit) = action()
}

@DocumentUpdateDsl
@Suppress("unused")
class CookingPropertyBuilder(basePath: String, updates: Record<String, Any?>, propertyName: String) :
    RecordPropertyUpdateBuilder<Cooking>(basePath, updates, propertyName) {
    val actorMeals = ActorMealPropertyBuilder(propertyName, updates, "actorMeals")
    val knownRecipes = PropertyUpdateBuilder<Array<String>>(propertyName, updates, "knownRecipes")
    val homebrewMeals = PropertyUpdateBuilder<Array<RecipeData>>(propertyName, updates, "homebrewMeals")
    val results = CookingResultPropertyBuilder(propertyName, updates, "results")
    val minimumSubsistence = PropertyUpdateBuilder<Int>(propertyName, updates, "minimumSubsistence")

    operator fun invoke(action: CookingPropertyBuilder.() -> Unit) = action()
}

@DocumentUpdateDsl
@Suppress("unused")
class RestingTrackPropertyBuilder(basePath: String, updates: Record<String, Any?>, propertyName: String) :
    RecordPropertyUpdateBuilder<Track?>(basePath, updates, propertyName) {
    val playlistUuid = PropertyUpdateBuilder<String>(propertyName, updates, "playlistUuid")
    val trackUuid = PropertyUpdateBuilder<String?>(propertyName, updates, "trackUuid")

    operator fun invoke(action: RestingTrackPropertyBuilder.() -> Unit) = action()
}

@DocumentUpdateDsl
@Suppress("unused")
class RegionSettingsPropertyBuilder(basePath: String, updates: Record<String, Any?>, propertyName: String) :
    RecordPropertyUpdateBuilder<RegionSettings>(basePath, updates, propertyName) {
    val regions = PropertyUpdateBuilder<String>(propertyName, updates, "regions")

    operator fun invoke(action: RegionSettingsPropertyBuilder.() -> Unit) = action()
}

@DocumentUpdateDsl
@Suppress("unused")
class RestSettingsPropertyBuilder(basePath: String, updates: Record<String, Any?>, propertyName: String) :
    RecordPropertyUpdateBuilder<RestSettings>(basePath, updates, propertyName) {
    val skipWatch = PropertyUpdateBuilder<Boolean>(propertyName, updates, "skipWatch")
    val skipDailyPreparations =
        PropertyUpdateBuilder<Boolean>(propertyName, updates, "skipDailyPreparations")
    val disableRandomEncounter =
        PropertyUpdateBuilder<Boolean>(propertyName, updates, "disableRandomEncounter")
    val skipWeather = PropertyUpdateBuilder<Boolean>(propertyName, updates, "skipWeather")

    operator fun invoke(action: RestSettingsPropertyBuilder.() -> Unit) = action()
}

@DocumentUpdateDsl
@Suppress("unused")
class CampingUpdateBuilder(val updates: Record<String, Any?>, basePath: String = "") {
    val actorUuids = PropertyUpdateBuilder<Array<String>>(basePath, updates, "actorUuids")
    val campingActivities =
        RecordPropertyUpdateBuilder<Record<String, CampingActivity>>(basePath, updates, "campingActivities")
    val cooking = CookingPropertyBuilder(basePath, updates, "cooking")
    val currentRegion = PropertyUpdateBuilder<String>(basePath, updates, "currentRegion")
    val homebrewCampingActivities =
        PropertyUpdateBuilder<Array<CampingActivityData>>(basePath, updates, "homebrewCampingActivities")
    val lockedActivities = PropertyUpdateBuilder<Array<String>>(basePath, updates, "lockedActivities")
    val watchSecondsRemaining = PropertyUpdateBuilder<Int>(basePath, updates, "watchSecondsRemaining")
    val gunsToClean = PropertyUpdateBuilder<Int>(basePath, updates, "gunsToClean")
    val dailyPrepsAtTime = PropertyUpdateBuilder<Int>(basePath, updates, "dailyPrepsAtTime")
    val encounterModifier = PropertyUpdateBuilder<Int>(basePath, updates, "encounterModifier")
    val restRollMode = PropertyUpdateBuilder<String>(basePath, updates, "restRollMode")
    val increaseWatchActorNumber = PropertyUpdateBuilder<Int>(basePath, updates, "increaseWatchActorNumber")
    val actorUuidsNotKeepingWatch = PropertyUpdateBuilder<Array<String>>(basePath, updates, "actorUuidsNotKeepingWatch")
    val alwaysPerformActivityIds = PropertyUpdateBuilder<Array<String>>(basePath, updates, "alwaysPerformActivityIds")
    val huntAndGatherTargetActorUuid = PropertyUpdateBuilder<String?>(basePath, updates, "huntAndGatherTargetActorUuid")
    val proxyRandomEncounterTableUuid =
        PropertyUpdateBuilder<String?>(basePath, updates, "proxyRandomEncounterTableUuid")
    val randomEncounterRollMode = PropertyUpdateBuilder<String>(basePath, updates, "randomEncounterRollMode")
    val ignoreSkillRequirements = PropertyUpdateBuilder<Boolean>(basePath, updates, "ignoreSkillRequirements")
    val minimumTravelSpeed = PropertyUpdateBuilder<Int?>(basePath, updates, "minimumTravelSpeed")
    val section = PropertyUpdateBuilder<String>(basePath, updates, "section")
    val worldSceneId = PropertyUpdateBuilder<String?>(basePath, updates, "worldSceneId")
    val autoApplyFatigued = PropertyUpdateBuilder<Boolean>(basePath, updates, "autoApplyFatigued")
    val secondsSpentTraveling = PropertyUpdateBuilder<Int>(basePath, updates, "secondsSpentTraveling")
    val secondsSpentHexploring = PropertyUpdateBuilder<Int>(basePath, updates, "secondsSpentHexploring")
    val resetTimeTrackingAfterOneDay = PropertyUpdateBuilder<Boolean>(basePath, updates, "resetTimeTrackingAfterOneDay")
    val travelModeActive = PropertyUpdateBuilder<Boolean>(basePath, updates, "travelModeActive")
    val restingTrack = RestingTrackPropertyBuilder(basePath, updates, "restingTrack")
    val regionSettings = RegionSettingsPropertyBuilder(basePath, updates, "regionSettings")
    val restSettings = RestSettingsPropertyBuilder(basePath, updates, "restSettings")
}

fun buildCampingUpdate(
    updates: Record<String, Any?> = unsafeJso(),
    block: CampingUpdateBuilder.() -> Unit
): Record<String, Any?> {
    val builder = CampingUpdateBuilder(updates)
    builder.block()
    return builder.updates
}

suspend fun CampingActor.typedCampingUpdate(block: CampingUpdateBuilder.(CampingData) -> Unit) {
    getCamping()?.let {
        val data = buildCampingUpdate { block(it) }
        console.log("Performing partial update", data)
        updateCamping(data)
    }
}