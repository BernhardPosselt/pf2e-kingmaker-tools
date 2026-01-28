package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.actions.ActionMessage
import at.posselt.pfrpg2e.actions.handlers.SyncActivitiesAction
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.asAnyObjectList
import at.posselt.pfrpg2e.utils.asSequence
import at.posselt.pfrpg2e.utils.buildPromise
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.Actor
import com.foundryvtt.core.documents.onPreUpdateActor
import com.foundryvtt.core.helpers.TypedHooks
import com.foundryvtt.core.utils.diffObject
import com.foundryvtt.core.utils.getProperty
import com.foundryvtt.core.utils.setProperty
import js.array.component1
import js.array.component2
import js.objects.Object
import js.objects.Record


class SyncActivities(
    val rollRandomEncounter: Boolean,
    val activities: Array<CampingActivityWithId>,
    val clearMealEffects: Boolean,
    val prepareCampsiteResult: String?
)

private data class ActivityChange(
    val previous: CampingActivityWithId? = null,
    val new: CampingActivityWithId,
    val resultChanged: Boolean,
    val data: CampingActivityData,
    val rollRandomEncounter: Boolean,
)

const val campingPath = "flags.${Config.moduleId}.camping-sheet"
private const val homebrewPath = "$campingPath.homebrewCampingActivities"
private const val alwaysPerformPath = "$campingPath.alwaysPerformActivityIds"
const val campingActivitiesPath = "$campingPath.campingActivities"

private val settingAttributes = setOf(
    homebrewPath,
    alwaysPerformPath,
)

private val listenForAttributeChanges = mapOf(
    campingActivitiesPath to ::campingActivitiesChanged,
    homebrewPath to ::homebrewCampingActivitiesChanged,
    alwaysPerformPath to ::alwaysPerformActivitiesChanged,
)

fun doObjectArraysDiffer(source: List<AnyObject>, target: List<AnyObject>): Boolean {
    return source.size != target.size ||
            (source.asSequence() zip target.asSequence())
                .any { (first, second) ->
                    Object.keys(diffObject(first, second)).isNotEmpty()
                }
}

private fun homebrewCampingActivitiesChanged(camping: CampingData, update: Any): Boolean {
    val current = camping.homebrewCampingActivities.sortedBy { it.id }
    val updateList = update.unsafeCast<Array<CampingActivityData>>().sortedBy { it.id }
    return doObjectArraysDiffer(current.asAnyObjectList(), updateList.asAnyObjectList())
}

private fun campingActivitiesChanged(camping: CampingData, update: Any): Boolean {
    val current = camping.campingActivitiesWithId().sortedBy { it.activityId }
    val updateList = update.unsafeCast<Record<String, CampingActivity>>().toCampingActivitiesWithId().sortedBy { it.activityId }
    return doObjectArraysDiffer(current.asAnyObjectList(), updateList.asAnyObjectList())
}

private fun alwaysPerformActivitiesChanged(camping: CampingData, update: Any): Boolean {
    val current = camping.alwaysPerformActivityIds.sorted()
    val updateList = update.unsafeCast<Array<String>>().sorted()
    return current != updateList
}

private fun relevantUpdate(camping: CampingData, update: AnyObject): Set<String> {
    return listenForAttributeChanges
        .mapNotNull { (key, entry) ->
            val updatedProperty = getProperty(update, key)
            updatedProperty?.let {
                if (entry(camping, updatedProperty)) {
                    key
                } else {
                    null
                }
            }
        }.toSet()
}

fun checkPreActorUpdate(actor: Actor, update: AnyObject): SyncActivities? {
    val camping = actor.takeIfInstance<CampingActor>()?.getCamping() ?: return null
    val updates = relevantUpdate(camping, update)
    if (updates.isEmpty()) return null
    console.log("Received camping update", update)
    val settingsChanged = updates.intersect(settingAttributes).isNotEmpty()
    val activities = getProperty(update, campingActivitiesPath)
        ?.unsafeCast<Record<String, CampingActivity>>()
        ?: camping.campingActivities
    val alwaysPerformActivities = getProperty(update, alwaysPerformPath)
        ?.unsafeCast<Array<String>>()
        ?: camping.alwaysPerformActivityIds
    val activitiesById = camping.campingActivitiesWithId().associateBy { it.activityId }
    val activityDataById = camping.getAllActivities().associateBy { it.id }
    val activityStateChanged = getActivityChanges(
        activities,
        activityDataById,
        activitiesById
    )
    val needsSync = settingsChanged
            || activityStateChanged.isNotEmpty()
            || Object.keys(camping.campingActivities).size != Object.keys(activities).size
    if (!needsSync) return null

    val prepareCampsiteChanged = prepareCampsiteChanged(activityStateChanged)
    val result = prepareCampsiteChanged?.parseResult()
    val prepareCampsiteResult = prepareCampsiteChanged?.let {
        val campResult = checkPrepareCampsiteResult(result)
        setSection(campResult, camping, update)
        campResult
    }
    return SyncActivities(
        rollRandomEncounter = activityStateChanged.any { it.resultChanged && it.rollRandomEncounter },
        activities = getActivitiesToSync(prepareCampsiteResult, alwaysPerformActivities, activities.toCampingActivitiesWithId()),
        clearMealEffects = result != null,
        prepareCampsiteResult = result?.toCamelCase()
    )
}


private fun getActivitiesToSync(
    prepareCampsiteResult: PrepareCampsiteResult?,
    alwaysPerformActivities: Array<String>,
    activities: Array<CampingActivityWithId>
) = if (prepareCampsiteResult == PrepareCampsiteResult.SKIP_CAMPING) {
    emptyArray()
} else {
    alwaysPerformActivities
        .map {
            CampingActivityWithId(
                activityId = it,
                actorUuid = null
            )
        }
        .toTypedArray() + activities.filter { it.actorUuid != null }
}

private fun setSection(
    prepareCampsiteResult: PrepareCampsiteResult?,
    camping: CampingData,
    update: AnyObject
) {
    val section = when (prepareCampsiteResult) {
        PrepareCampsiteResult.SKIP_CAMPING -> CampingSheetSection.PREPARE_CAMPSITE.toCamelCase()
        PrepareCampsiteResult.CAMPING_ACTIVITIES -> CampingSheetSection.CAMPING_ACTIVITIES.toCamelCase()
        null -> camping.section
    }
    setProperty(
        update,
        "$campingPath.section",
        section,
    )
}

private fun getActivityChanges(
    activities: Record<String, CampingActivity>,
    activityDataById: Map<String, CampingActivityData>,
    activitiesById: Map<String, CampingActivityWithId>,
): List<ActivityChange> {
    return activities.asSequence().mapNotNull { (newActivityId, new) ->
        val data = activityDataById[newActivityId]
        val previous = activitiesById[newActivityId]
        val hasDifferentResult = previous != null && (new.result != previous.result)
        val hasDifferentActor = previous != null && (new.actorUuid != previous.actorUuid)
        if (data != null && (hasDifferentActor || hasDifferentResult)) {
            val rollRandomEncounter = new.parseResult()
                ?.let { data.getOutcome(it) }
                ?.checkRandomEncounter == true
            ActivityChange(
                previous = previous,
                new = CampingActivityWithId(
                    activityId = newActivityId,
                    actorUuid = new.actorUuid,
                    result = new.result,
                    selectedSkill = new.selectedSkill,
                ),
                data = data,
                resultChanged = hasDifferentResult,
                rollRandomEncounter = hasDifferentResult && rollRandomEncounter
            )
        } else {
            null
        }
    }.toList()
}

private enum class PrepareCampsiteResult {
    SKIP_CAMPING,
    CAMPING_ACTIVITIES,
}

private fun checkPrepareCampsiteResult(result: DegreeOfSuccess?): PrepareCampsiteResult {
    return if (result == null || result == DegreeOfSuccess.CRITICAL_FAILURE) {
        PrepareCampsiteResult.SKIP_CAMPING
    } else {
        PrepareCampsiteResult.CAMPING_ACTIVITIES
    }
}

private fun prepareCampsiteChanged(activityStateChanged: List<ActivityChange>) =
    activityStateChanged
        .map { it.new }
        .find { it.activityId == prepareCampsiteId }

fun registerActivityDiffingHooks(game: Game, dispatcher: ActionDispatcher) {
    TypedHooks.onPreUpdateActor { actor, update, _, _ ->
        checkPreActorUpdate(actor, update)?.let {
            buildPromise {
                dispatcher.dispatch(
                    ActionMessage(
                        action = "syncActivities",
                        data = SyncActivitiesAction(
                            rollRandomEncounter = it.rollRandomEncounter,
                            activities = it.activities,
                            clearMealEffects = it.clearMealEffects,
                            prepareCampsiteResult = it.prepareCampsiteResult,
                            campingActorUuid = actor.uuid,
                        ).unsafeCast<AnyObject>()
                    )
                )
            }
        }
    }
}