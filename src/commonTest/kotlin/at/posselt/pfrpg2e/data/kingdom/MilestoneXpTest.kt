package at.posselt.pfrpg2e.data.kingdom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MilestoneXpTest {

    @Test
    fun defaultValuesMatchWorkbookRows() {
        assertEquals(14, milestoneXpAwards.size)
        assertEquals(40, milestoneXpAwards[0].xpAward)
        assertEquals("Claim your first Landmark", milestoneXpAwards[0].milestone)
        assertEquals(120, milestoneXpAwards.last().xpAward)
        assertEquals("Reach kingdom Size 100", milestoneXpAwards.last().milestone)
    }

    @Test
    fun xpAwardsMatchExpectedDistribution() {
        val xp40 = milestoneXpAwards.filter { it.xpAward == 40 }
        val xp60 = milestoneXpAwards.filter { it.xpAward == 60 }
        val xp80 = milestoneXpAwards.filter { it.xpAward == 80 }
        val xp120 = milestoneXpAwards.filter { it.xpAward == 120 }
        assertEquals(4, xp40.size, "Expected 4 milestones at 40 XP")
        assertEquals(4, xp60.size, "Expected 4 milestones at 60 XP")
        assertEquals(4, xp80.size, "Expected 4 milestones at 80 XP")
        assertEquals(2, xp120.size, "Expected 2 milestones at 120 XP")
    }

    @Test
    fun allMilestonesAreNonBlank() {
        milestoneXpAwards.forEach {
            assertFalse(it.milestone.isBlank(), "Milestone should not be blank")
        }
    }

    @Test
    fun allXpAwardsArePositive() {
        milestoneXpAwards.forEach {
            assertTrue(it.xpAward > 0, "XP award should be positive, got ${it.xpAward}")
        }
    }

    @Test
    fun noDuplicateMilestoneNames() {
        val names = milestoneXpAwards.map { it.milestone }
        assertEquals(names.size, names.toSet().size, "Milestone names should be unique")
    }

    @Test
    fun milestonesOrderedByXpAwardThenRowOrder() {
        var lastXp = 0
        milestoneXpAwards.forEach {
            assertTrue(
                it.xpAward >= lastXp,
                "Milestones should be ordered by ascending XP: ${it.milestone} (${it.xpAward}) after $lastXp"
            )
            lastXp = it.xpAward
        }
    }

    @Test
    fun copyPreservesAllFields() {
        val original = milestoneXpAwards[0]
        val copied = original.copy()
        assertEquals(original.xpAward, copied.xpAward)
        assertEquals(original.milestone, copied.milestone)
    }

    @Test
    fun copyAllowsFieldOverride() {
        val original = milestoneXpAwards[0]
        val modifiedXp = original.copy(xpAward = 999)
        assertEquals(999, modifiedXp.xpAward)
        assertEquals(original.milestone, modifiedXp.milestone)

        val modifiedMilestone = original.copy(milestone = "Test Milestone")
        assertEquals(original.xpAward, modifiedMilestone.xpAward)
        assertEquals("Test Milestone", modifiedMilestone.milestone)
    }

    @Test
    fun equalsWorks() {
        val a = MilestoneXp(xpAward = 40, milestone = "Claim your first Landmark")
        val b = MilestoneXp(xpAward = 40, milestone = "Claim your first Landmark")
        assertEquals(a, b)
    }

    @Test
    fun equalsDetectsXpDifference() {
        val a = MilestoneXp(xpAward = 40, milestone = "Same Milestone")
        val b = MilestoneXp(xpAward = 60, milestone = "Same Milestone")
        assertNotEquals(a, b)
    }

    @Test
    fun equalsDetectsMilestoneDifference() {
        val a = MilestoneXp(xpAward = 40, milestone = "Milestone A")
        val b = MilestoneXp(xpAward = 40, milestone = "Milestone B")
        assertNotEquals(a, b)
    }

    @Test
    fun hashCodeConsistentWithEquals() {
        val a = MilestoneXp(xpAward = 80, milestone = "Expand a town into your first city")
        val b = MilestoneXp(xpAward = 80, milestone = "Expand a town into your first city")
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun toStringContainsBothFields() {
        val entry = MilestoneXp(xpAward = 40, milestone = "Claim your first Landmark")
        val str = entry.toString()
        assertTrue(str.contains("40"), "toString should contain xpAward value")
        assertTrue(str.contains("Claim your first Landmark"), "toString should contain milestone value")
        assertTrue(str.contains("MilestoneXp"), "toString should contain class name")
    }

    @Test
    fun peakXpValuesAtBoundaries() {
        assertEquals(40, milestoneXpAwards.minOf { it.xpAward })
        assertEquals(120, milestoneXpAwards.maxOf { it.xpAward })
    }

    @Test
    fun specificMilestoneLookup() {
        val landmark = milestoneXpAwards.single { it.milestone == "Claim your first Landmark" }
        assertEquals(40, landmark.xpAward)

        val size100 = milestoneXpAwards.single { it.milestone == "Reach kingdom Size 100" }
        assertEquals(120, size100.xpAward)

        val metropolis = milestoneXpAwards.single {
            it.milestone == "Expand a city into your first metropolis"
        }
        assertEquals(120, metropolis.xpAward)
    }
}
