package at.posselt.pfrpg2e.camping

import js.objects.unsafeJso
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompanionGatingTest {

    private fun activityWithCompanion(companionName: String?): CampingActivityData = unsafeJso {
        id = "test-activity"
        name = "campingActivities.test-activity.name"
        skills = emptyArray<CampingSkill>()
        isSecret = false
        isLocked = false
        isHomebrew = false
        oncePerSession = true
        requiredCompanion = companionName
    }

    // === Companion present ===

    @Test
    fun testCompanionPresentExactMatch() {
        val activity = activityWithCompanion("Harrim")
        assertTrue(activity.isRequiredCompanionPresent(setOf("Harrim", "Linzi", "Amiri")))
    }

    @Test
    fun testCompanionPresentCaseInsensitive() {
        val activity = activityWithCompanion("harrim")
        assertTrue(activity.isRequiredCompanionPresent(setOf("Harrim")))
    }

    @Test
    fun testCompanionPresentCaseInsensitiveReversed() {
        val activity = activityWithCompanion("Harrim")
        assertTrue(activity.isRequiredCompanionPresent(setOf("harrim")))
    }

    @Test
    fun testCompanionPresentAmongMany() {
        val activity = activityWithCompanion("Tristian")
        assertTrue(
            activity.isRequiredCompanionPresent(
                setOf("Harrim", "Linzi", "Amiri", "Tristian", "Valerie")
            )
        )
    }

    // === Companion absent ===

    @Test
    fun testCompanionAbsentFromCamp() {
        val activity = activityWithCompanion("Harrim")
        assertFalse(activity.isRequiredCompanionPresent(setOf("Linzi", "Amiri")))
    }

    @Test
    fun testCompanionAbsentEmptyCamp() {
        val activity = activityWithCompanion("Harrim")
        assertFalse(activity.isRequiredCompanionPresent(emptySet()))
    }

    @Test
    fun testCompanionAbsentSimilarName() {
        val activity = activityWithCompanion("Harrim")
        assertFalse(activity.isRequiredCompanionPresent(setOf("Harrimus")))
    }

    @Test
    fun testCompanionAbsentSubstringName() {
        val activity = activityWithCompanion("Harrim")
        assertFalse(activity.isRequiredCompanionPresent(setOf("Har")))
    }

    // === No companion required ===

    @Test
    fun testNoCompanionRequiredWithActor() {
        val activity = activityWithCompanion(null)
        assertTrue(activity.isRequiredCompanionPresent(setOf("Linzi")))
    }

    @Test
    fun testNoCompanionRequiredEmptyCamp() {
        val activity = activityWithCompanion(null)
        assertTrue(activity.isRequiredCompanionPresent(emptySet()))
    }

    // === Real activity IDs ===

    @Test
    fun testBlendIntoNightRequiresHarrim() {
        val activity = activityWithCompanion("Harrim")
        assertFalse(activity.isRequiredCompanionPresent(setOf("Linzi", "Amiri")))
        assertTrue(activity.isRequiredCompanionPresent(setOf("Harrim")))
    }

    @Test
    fun testBolsterConfidenceRequiresLinzi() {
        val activity = activityWithCompanion("Linzi")
        assertFalse(activity.isRequiredCompanionPresent(setOf("Harrim", "Amiri")))
        assertTrue(activity.isRequiredCompanionPresent(setOf("Linzi")))
    }

    @Test
    fun testSetAlarmsRequiresOctavia() {
        val activity = activityWithCompanion("Octavia")
        assertFalse(activity.isRequiredCompanionPresent(setOf("Harrim", "Amiri")))
        assertTrue(activity.isRequiredCompanionPresent(setOf("Octavia")))
    }

    // === Lock bypass for companion activities ===
    // Companion activities ship with isLocked=true so they default into lockedActivities.
    // They must still appear (greyed out via presence gating) rather than be hidden.

    @Test
    fun testCompanionActivityNotHiddenWhenLocked() {
        val activity = activityWithCompanion("Amiri")
        assertFalse(activity.isHiddenByLock(setOf("test-activity")))
    }

    @Test
    fun testCompanionActivityNotHiddenWhenAbsentFromLockSet() {
        val activity = activityWithCompanion("Amiri")
        assertFalse(activity.isHiddenByLock(emptySet()))
    }

    @Test
    fun testNonCompanionActivityHiddenWhenLocked() {
        val activity = activityWithCompanion(null)
        assertTrue(activity.isHiddenByLock(setOf("test-activity")))
    }

    @Test
    fun testNonCompanionActivityShownWhenUnlocked() {
        val activity = activityWithCompanion(null)
        assertFalse(activity.isHiddenByLock(setOf("some-other-activity")))
    }
}
