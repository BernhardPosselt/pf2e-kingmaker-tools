package at.posselt.pfrpg2e.data.kingdom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ArmyTypeTest {

    @Test
    fun allArmyTypesExist() {
        val values = ArmyType.entries
        assertTrue(values.contains(ArmyType.CAVALRY))
        assertTrue(values.contains(ArmyType.INFANTRY))
        assertTrue(values.contains(ArmyType.SIEGE))
        assertTrue(values.contains(ArmyType.SKIRMISHER))
        assertTrue(values.contains(ArmyType.UNIQUE))
    }

    @Test
    fun expectedNumberOfArmyTypes() {
        assertEquals(5, ArmyType.entries.size)
    }
}

class ArmyStatsDataTest {

    @Test
    fun tableHasTwentyRows() {
        assertEquals(20, armyStatsData.size, "Army stats table should have 20 rows (levels 1-20)")
    }

    @Test
    fun firstRowMatchesLevel1() {
        val level1 = armyStatsData.first()
        assertEquals(1, level1.level)
        assertEquals(7, level1.scouting)
        assertEquals(15, level1.standardDc)
        assertEquals(16, level1.ac)
        assertEquals(10, level1.highSave)
        assertEquals(4, level1.lowSave)
        assertEquals(9, level1.attack)
        assertEquals(1, level1.maxTactics)
    }

    @Test
    fun lastRowMatchesLevel20() {
        val level20 = armyStatsData.last()
        assertEquals(20, level20.level)
        assertEquals(33, level20.scouting)
        assertEquals(40, level20.standardDc)
        assertEquals(45, level20.ac)
        assertEquals(36, level20.highSave)
        assertEquals(30, level20.lowSave)
        assertEquals(38, level20.attack)
        assertEquals(6, level20.maxTactics)
    }

    @Test
    fun levelsAreSequential() {
        armyStatsData.forEachIndexed { index, stats ->
            assertEquals(index + 1, stats.level, "ArmyStats level at index $index should be ${index + 1}")
        }
    }

    @Test
    fun scoutingIncreasesWithLevel() {
        for (i in 1 until armyStatsData.size) {
            assertTrue(
                armyStatsData[i].scouting >= armyStatsData[i - 1].scouting,
                "Scouting should not decrease from level ${i} to ${i + 1}"
            )
        }
    }

    @Test
    fun acIncreasesWithLevel() {
        for (i in 1 until armyStatsData.size) {
            assertTrue(
                armyStatsData[i].ac >= armyStatsData[i - 1].ac,
                "AC should not decrease from level ${i} to ${i + 1}"
            )
        }
    }

    @Test
    fun maxTacticsIncreasesAtExpectedLevels() {
        assertEquals(1, armyStatsData[0].maxTactics)  // level 1
        assertEquals(1, armyStatsData[1].maxTactics)  // level 2
        assertEquals(1, armyStatsData[2].maxTactics)  // level 3
        assertEquals(2, armyStatsData[3].maxTactics)  // level 4
        assertEquals(3, armyStatsData[7].maxTactics)  // level 8
        assertEquals(4, armyStatsData[11].maxTactics) // level 12
        assertEquals(5, armyStatsData[15].maxTactics) // level 16
        assertEquals(6, armyStatsData[19].maxTactics) // level 20
    }
}

class FindArmyStatsTest {

    @Test
    fun findsLevel1() {
        val result = findArmyStats(1)
        assertNotNull(result)
        assertEquals(1, result.level)
    }

    @Test
    fun findsLevel10() {
        val result = findArmyStats(10)
        assertNotNull(result)
        assertEquals(10, result.level)
        assertEquals(19, result.scouting)
    }

    @Test
    fun findsLevel20() {
        val result = findArmyStats(20)
        assertNotNull(result)
        assertEquals(20, result.level)
    }

    @Test
    fun returnsNullForLevel0() {
        val result = findArmyStats(0)
        assertNull(result)
    }

    @Test
    fun returnsNullForLevel21() {
        val result = findArmyStats(21)
        assertNull(result)
    }

    @Test
    fun returnsNullForNegativeLevel() {
        val result = findArmyStats(-1)
        assertNull(result)
    }
}

class ArmyStatsDataClassTest {

    @Test
    fun copyPreservesAllFields() {
        val original = ArmyStats(level = 5, scouting = 12, standardDc = 20, ac = 22, highSave = 15, lowSave = 9, attack = 15, maxTactics = 2)
        val copied = original.copy()
        assertEquals(original.level, copied.level)
        assertEquals(original.scouting, copied.scouting)
        assertEquals(original.standardDc, copied.standardDc)
        assertEquals(original.ac, copied.ac)
        assertEquals(original.highSave, copied.highSave)
        assertEquals(original.lowSave, copied.lowSave)
        assertEquals(original.attack, copied.attack)
        assertEquals(original.maxTactics, copied.maxTactics)
    }

    @Test
    fun equalsWorks() {
        val a = ArmyStats(level = 5, scouting = 12, standardDc = 20, ac = 22, highSave = 15, lowSave = 9, attack = 15, maxTactics = 2)
        val b = ArmyStats(level = 5, scouting = 12, standardDc = 20, ac = 22, highSave = 15, lowSave = 9, attack = 15, maxTactics = 2)
        assertEquals(a, b)
    }

    @Test
    fun equalsDetectsDifference() {
        val a = ArmyStats(level = 5, scouting = 12, standardDc = 20, ac = 22, highSave = 15, lowSave = 9, attack = 15, maxTactics = 2)
        val b = ArmyStats(level = 6, scouting = 14, standardDc = 22, ac = 24, highSave = 17, lowSave = 11, attack = 17, maxTactics = 2)
        kotlin.test.assertNotEquals(a, b)
    }

    @Test
    fun hashCodeConsistentWithEquals() {
        val a = ArmyStats(level = 5, scouting = 12, standardDc = 20, ac = 22, highSave = 15, lowSave = 9, attack = 15, maxTactics = 2)
        val b = ArmyStats(level = 5, scouting = 12, standardDc = 20, ac = 22, highSave = 15, lowSave = 9, attack = 15, maxTactics = 2)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun toStringContainsClassName() {
        val stats = ArmyStats(level = 1, scouting = 7, standardDc = 15, ac = 16, highSave = 10, lowSave = 4, attack = 9, maxTactics = 1)
        val str = stats.toString()
        assertTrue(str.contains("ArmyStats"))
    }
}
