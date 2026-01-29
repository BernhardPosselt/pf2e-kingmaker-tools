package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.actions.ActionMessage
import at.posselt.pfrpg2e.actions.handlers.SyncActivitiesAction
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.Changes
import at.posselt.pfrpg2e.utils.asAnyObject
import at.posselt.pfrpg2e.utils.asSequence
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.filterObject
import at.posselt.pfrpg2e.utils.parseChanges
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.Actor
import com.foundryvtt.core.documents.onPreUpdateActor
import com.foundryvtt.core.helpers.TypedHooks
import com.foundryvtt.core.utils.getProperty
import com.foundryvtt.core.utils.objectsEqual
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

private val settingAttributes = setOf(
    "homebrewCampingActivities",
    "alwaysPerformActivityIds",
)

private val relevantAttributes = settingAttributes + "campingActivities"

fun Changes.settingsChanged() = !objectsEqual(
    filterObject(filteredApplied, settingAttributes),
    filterObject(filteredOriginal, settingAttributes),
)

fun checkPreActorUpdate(actor: Actor, update: AnyObject): SyncActivities? {
    val camping = actor.takeIfInstance<CampingActor>()?.getCamping() ?: return null
    val campingUpdate = getProperty(update, campingPath).unsafeCast<AnyObject?>() ?: return null
    val changes = parseChanges(camping.asAnyObject(), campingUpdate, relevantAttributes) ?: return null
    console.log("Received camping update", update, changes.filteredOriginal, changes.filteredApplied)
    val newActivities = getProperty(changes.filteredApplied, "campingActivities")
        ?.unsafeCast<Record<String, CampingActivity>>()
        ?: return null
    val alwaysPerformActivities = getProperty(changes.filteredApplied, "alwaysPerformActivityIds")
        ?.unsafeCast<Array<String>>()
        ?: return null
    val activityChanges = getActivityChanges(newActivities, camping)
    val needsSync = changes.settingsChanged()
            || activityChanges.isNotEmpty()
            || Object.keys(camping.campingActivities).size != Object.keys(newActivities).size
    if (!needsSync) return null
    val prepareCampsiteChanged = prepareCampsiteChanged(activityChanges)
    val result = prepareCampsiteChanged?.parseResult()
    val prepareCampsiteResult = prepareCampsiteChanged?.let {
        val campResult = checkPrepareCampsiteResult(result)
        setSection(campResult, camping, update)
        campResult
    }
    return SyncActivities(
        rollRandomEncounter = activityChanges.any { it.resultChanged && it.rollRandomEncounter },
        activities = getActivitiesToSync(
            prepareCampsiteResult,
            alwaysPerformActivities,
            newActivities.toCampingActivitiesWithId()
        ),
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
    newActivities: Record<String, CampingActivity>,
    oldCamping: CampingData,
): List<ActivityChange> {
    val activityDataById = oldCamping.getAllActivities().associateBy { it.id }
    val oldActivitiesById = oldCamping.campingActivitiesWithId().associateBy { it.activityId }
    return newActivities.asSequence().mapNotNull { (newActivityId, new) ->
        val data = activityDataById[newActivityId]
        val previous = oldActivitiesById[newActivityId]
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