package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.fromCamelCase

/**
 * Validates camping activity scheduling rules.
 *
 * Rules enforced:
 * - Each activity takes 2 hours.
 * - Maximum downtime budget is 8 hours (4 activities per PC per session).
 * - No two PCs may attempt the same camping activity at the same time.
 * - Once an activity has at least a success (oncePerSession=true), that activity
 *   cannot be attempted again by any PC until the next camping session.
 * - Activities with oncePerSession=false and no success lockout can be retried.
 */
object CampingActivityScheduler {

    const val DOWNTIME_HOURS_PER_ACTIVITY = 2
    const val MAX_DOWNTIME_HOURS = 8
    const val MAX_ACTIVITIES_PER_PC = MAX_DOWNTIME_HOURS / DOWNTIME_HOURS_PER_ACTIVITY // 4

    /**
     * Result of a scheduling validation.
     */
    sealed class SchedulingResult {
        data object Allowed : SchedulingResult()
        data class Blocked(val reason: String) : SchedulingResult()
    }

    /**
     * Count how many activities a given actor UUID has been assigned to in the current session.
     */
    fun countActivitiesForActor(activities: Array<CampingActivityWithId>, actorUuid: String): Int =
        activities.count { it.actorUuid == actorUuid }

    /**
     * Check whether the actor has exceeded the 8-hour downtime budget (4 activities).
     */
    fun isOverDowntimeBudget(activities: Array<CampingActivityWithId>, actorUuid: String): Boolean =
        countActivitiesForActor(activities, actorUuid) >= MAX_ACTIVITIES_PER_PC

    /**
     * Check whether a different PC is currently attempting the same activity.
     *
     * Only unresolved attempts (no [CampingActivityWithId.result] yet) count as "at the same
     * time" — once an attempt has been resolved (success or failure), the activity slot is
     * freed and the success-lockout rule ([hasActivitySucceeded]) governs re-attempts.
     */
    fun isActivityAlreadyAttempted(
        activities: Array<CampingActivityWithId>,
        activityId: String,
        requesterActorUuid: String
    ): Boolean =
        activities.any {
            it.activityId == activityId &&
                it.actorUuid != null &&
                it.actorUuid != requesterActorUuid &&
                it.result == null
        }

    /**
     * Check whether an activity has already achieved at least a success result.
     */
    fun hasActivitySucceeded(activities: Array<CampingActivityWithId>, activityId: String): Boolean {
        val activity = activities.find { it.activityId == activityId } ?: return false
        val result = activity.result?.let { fromCamelCase<DegreeOfSuccess>(it) } ?: return false
        return result == DegreeOfSuccess.SUCCESS || result == DegreeOfSuccess.CRITICAL_SUCCESS
    }

    /**
     * Validate whether a PC can be assigned to an activity.
     *
     * @param activities Current array of all camping activities with their assignments
     * @param activityData The activity data for the activity being assigned
     * @param actorUuid The UUID of the PC attempting the activity
     * @return [SchedulingResult.Allowed] if the assignment is valid, or [SchedulingResult.Blocked] with a reason
     */
    fun canAssign(
        activities: Array<CampingActivityWithId>,
        activityData: CampingActivityData,
        actorUuid: String
    ): SchedulingResult {
        // Rule: max 8 hours downtime budget (4 activities per PC)
        if (isOverDowntimeBudget(activities, actorUuid)) {
            return SchedulingResult.Blocked(
                "Actor has already been assigned to $MAX_ACTIVITIES_PER_PC activities (8-hour downtime limit reached)"
            )
        }

        // Rule: no two PCs may attempt the same camping activity at the same time
        if (isActivityAlreadyAttempted(activities, activityData.id, actorUuid)) {
            return SchedulingResult.Blocked(
                "Another PC is already assigned to activity '${activityData.name}'"
            )
        }

        // Rule: once an activity has at least a success, it cannot be attempted again
        // (unless oncePerSession is false)
        if (activityData.oncePerSession && hasActivitySucceeded(activities, activityData.id)) {
            return SchedulingResult.Blocked(
                "Activity '${activityData.name}' has already succeeded and cannot be attempted again this session"
            )
        }

        return SchedulingResult.Allowed
    }
}
