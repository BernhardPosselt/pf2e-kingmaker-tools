package at.posselt.pfrpg2e.camping

import js.objects.unsafeJso
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CampingActivitySchedulerTest {

    private val testActivity: CampingActivityData = unsafeJso {
        id = "test-activity"
        name = "campingActivities.test-activity.name"
        skills = arrayOf(CampingSkill(
            name = "survival",
            proficiency = "trained",
            dcType = "zone",
        ))
        isSecret = false
        isLocked = false
        isHomebrew = false
        oncePerSession = true
        requiredCompanion = null
    }

    private val repeatableActivity: CampingActivityData = unsafeJso {
        id = "relax"
        name = "campingActivities.relax.name"
        skills = emptyArray<CampingSkill>()
        isSecret = false
        isLocked = false
        isHomebrew = false
        oncePerSession = false
        requiredCompanion = null
    }

    private val anotherActivity: CampingActivityData = unsafeJso {
        id = "another-activity"
        name = "campingActivities.another-activity.name"
        skills = arrayOf(CampingSkill(
            name = "perception",
            proficiency = "expert",
            dcType = "zone",
        ))
        isSecret = false
        isLocked = false
        isHomebrew = false
        oncePerSession = true
        requiredCompanion = null
    }

    private fun activityWithId(
        activityId: String,
        actorUuid: String? = null,
        result: String? = null,
        selectedSkill: String? = null,
    ) = CampingActivityWithId(
        activityId = activityId,
        actorUuid = actorUuid,
        result = result,
        selectedSkill = selectedSkill,
    )

    // === 8-hour downtime budget tests ===

    @Test
    fun testActorUnderBudgetIsAllowed() {
        val activities = arrayOf(
            activityWithId("test-activity", actorUuid = "actor-1"),
            activityWithId("another-activity", actorUuid = "actor-1"),
        )
        val result = CampingActivityScheduler.canAssign(activities, testActivity, "actor-1")
        assertTrue(result is CampingActivityScheduler.SchedulingResult.Allowed)
    }

    @Test
    fun testActorAtExactBudgetIsBlocked() {
        val activities = arrayOf(
            activityWithId("test-activity", actorUuid = "actor-1"),
            activityWithId("another-activity", actorUuid = "actor-1"),
            activityWithId("relax", actorUuid = "actor-1"),
            activityWithId("hunt-and-gather", actorUuid = "actor-1"),
        )
        val result = CampingActivityScheduler.canAssign(activities, testActivity, "actor-1")
        assertTrue(result is CampingActivityScheduler.SchedulingResult.Blocked)
    }

    @Test
    fun testActorOverBudgetIsBlocked() {
        val activities = arrayOf(
            activityWithId("test-activity", actorUuid = "actor-1"),
            activityWithId("another-activity", actorUuid = "actor-1"),
            activityWithId("relax", actorUuid = "actor-1"),
            activityWithId("hunt-and-gather", actorUuid = "actor-1"),
            activityWithId("organize-watch", actorUuid = "actor-1"),
        )
        val result = CampingActivityScheduler.canAssign(activities, testActivity, "actor-1")
        assertTrue(result is CampingActivityScheduler.SchedulingResult.Blocked)
    }

    @Test
    fun testBudgetCountsOnlyOwnActivities() {
        val activities = arrayOf(
            activityWithId("test-activity", actorUuid = "actor-1"),
            activityWithId("another-activity", actorUuid = "actor-1"),
            activityWithId("relax", actorUuid = "actor-2"),
            activityWithId("hunt-and-gather", actorUuid = "actor-2"),
            activityWithId("organize-watch", actorUuid = "actor-2"),
            activityWithId("camouflage-campsite", actorUuid = "actor-2"),
        )
        val result = CampingActivityScheduler.canAssign(activities, testActivity, "actor-1")
        assertTrue(result is CampingActivityScheduler.SchedulingResult.Allowed)
    }

    // === Duplicate same-time attempt tests ===

    @Test
    fun testSameActivityByDifferentActorIsBlocked() {
        val activities = arrayOf(
            activityWithId("test-activity", actorUuid = "actor-1"),
        )
        val result = CampingActivityScheduler.canAssign(activities, testActivity, "actor-2")
        assertTrue(result is CampingActivityScheduler.SchedulingResult.Blocked)
    }

    @Test
    fun testSameActivityBySameActorIsAllowed() {
        val activities = arrayOf(
            activityWithId("test-activity", actorUuid = "actor-1"),
        )
        val result = CampingActivityScheduler.canAssign(activities, testActivity, "actor-1")
        assertTrue(result is CampingActivityScheduler.SchedulingResult.Allowed)
    }

    @Test
    fun testDifferentActivitiesByDifferentActorsIsAllowed() {
        val activities = arrayOf(
            activityWithId("test-activity", actorUuid = "actor-1"),
        )
        val result = CampingActivityScheduler.canAssign(activities, anotherActivity, "actor-2")
        assertTrue(result is CampingActivityScheduler.SchedulingResult.Allowed)
    }

    @Test
    fun testUnassignedActivityIsAllowed() {
        val activities = arrayOf(
            activityWithId("test-activity", actorUuid = null),
        )
        val result = CampingActivityScheduler.canAssign(activities, testActivity, "actor-1")
        assertTrue(result is CampingActivityScheduler.SchedulingResult.Allowed)
    }

    // === Success lockout tests ===

    @Test
    fun testSuccessfulOncePerSessionActivityIsBlocked() {
        val activities = arrayOf(
            activityWithId("test-activity", actorUuid = "actor-1", result = "success"),
        )
        val result = CampingActivityScheduler.canAssign(activities, testActivity, "actor-2")
        assertTrue(result is CampingActivityScheduler.SchedulingResult.Blocked)
    }

    @Test
    fun testCriticalSuccessfulOncePerSessionActivityIsBlocked() {
        val activities = arrayOf(
            activityWithId("test-activity", actorUuid = "actor-1", result = "criticalSuccess"),
        )
        val result = CampingActivityScheduler.canAssign(activities, testActivity, "actor-2")
        assertTrue(result is CampingActivityScheduler.SchedulingResult.Blocked)
    }

    @Test
    fun testFailedOncePerSessionActivityIsAllowed() {
        val activities = arrayOf(
            activityWithId("test-activity", actorUuid = "actor-1", result = "failure"),
        )
        val result = CampingActivityScheduler.canAssign(activities, testActivity, "actor-2")
        assertTrue(result is CampingActivityScheduler.SchedulingResult.Allowed)
    }

    @Test
    fun testCriticalFailedOncePerSessionActivityIsAllowed() {
        val activities = arrayOf(
            activityWithId("test-activity", actorUuid = "actor-1", result = "criticalFailure"),
        )
        val result = CampingActivityScheduler.canAssign(activities, testActivity, "actor-2")
        assertTrue(result is CampingActivityScheduler.SchedulingResult.Allowed)
    }

    @Test
    fun testRepeatableActivityIsNotLockedBySuccess() {
        val activities = arrayOf(
            activityWithId("relax", actorUuid = "actor-1", result = "success"),
        )
        val result = CampingActivityScheduler.canAssign(activities, repeatableActivity, "actor-2")
        assertTrue(result is CampingActivityScheduler.SchedulingResult.Allowed)
    }

    // === Count activities tests ===

    @Test
    fun testCountActivitiesForActor() {
        val activities = arrayOf(
            activityWithId("test-activity", actorUuid = "actor-1"),
            activityWithId("another-activity", actorUuid = "actor-1"),
            activityWithId("relax", actorUuid = "actor-2"),
        )
        assertEquals(2, CampingActivityScheduler.countActivitiesForActor(activities, "actor-1"))
        assertEquals(1, CampingActivityScheduler.countActivitiesForActor(activities, "actor-2"))
        assertEquals(0, CampingActivityScheduler.countActivitiesForActor(activities, "actor-3"))
    }

    // === Edge cases ===

    @Test
    fun testEmptyActivitiesAllowsAssignment() {
        val activities = emptyArray<CampingActivityWithId>()
        val result = CampingActivityScheduler.canAssign(activities, testActivity, "actor-1")
        assertTrue(result is CampingActivityScheduler.SchedulingResult.Allowed)
    }

    @Test
    fun testActivityWithNullActorDoesNotBlock() {
        val activities = arrayOf(
            activityWithId("test-activity", actorUuid = null, result = null),
        )
        val result = CampingActivityScheduler.canAssign(activities, testActivity, "actor-1")
        assertTrue(result is CampingActivityScheduler.SchedulingResult.Allowed)
    }

    @Test
    fun testActivityWithNullResultDoesNotLock() {
        val activities = arrayOf(
            activityWithId("test-activity", actorUuid = "actor-1", result = null),
        )
        val result = CampingActivityScheduler.canAssign(activities, testActivity, "actor-2")
        // Blocked because another actor is assigned, not because of success lockout
        assertTrue(result is CampingActivityScheduler.SchedulingResult.Blocked)
    }

    @Test
    fun testMaxActivitiesConstant() {
        assertEquals(4, CampingActivityScheduler.MAX_ACTIVITIES_PER_PC)
    }

    @Test
    fun testDowntimeHoursConstant() {
        assertEquals(2, CampingActivityScheduler.DOWNTIME_HOURS_PER_ACTIVITY)
        assertEquals(8, CampingActivityScheduler.MAX_DOWNTIME_HOURS)
    }

    // === Downtime hours assignment cap ===

    @Test
    fun testAssignmentCapBlocksFifthActivityEvenWhenUnrolled() {
        // The 4-activity cap gates assignment regardless of whether checks have been rolled.
        val activities = arrayOf(
            activityWithId("test-activity", actorUuid = "actor-1", result = null),
            activityWithId("another-activity", actorUuid = "actor-1", result = null),
            activityWithId("relax", actorUuid = "actor-1", result = null),
            activityWithId("hunt-and-gather", actorUuid = "actor-1", result = null),
        )
        assertTrue(CampingActivityScheduler.isOverDowntimeBudget(activities, "actor-1"))
    }
}
