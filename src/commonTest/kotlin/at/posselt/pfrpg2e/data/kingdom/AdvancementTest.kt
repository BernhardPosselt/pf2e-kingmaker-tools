package at.posselt.pfrpg2e.data.kingdom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AdvancementTest {

    @Test
    fun rowCountMatchesWorkbook() {
        assertEquals(20, advancementData.size, "Workbook has exactly 20 advancement rows (levels 1-20)")
    }

    @Test
    fun firstRowMatchesWorkbook() {
        val level1 = advancementData.first()
        assertEquals(1, level1.level)
        assertEquals(14, level1.controlDc)
        assertEquals(
            "Charter, government, heartland, initial proficiencies, favored land, settlement construction (village)",
            level1.kingdomFeatures,
        )
    }

    @Test
    fun lastRowMatchesWorkbook() {
        val level20 = advancementData.last()
        assertEquals(20, level20.level)
        assertEquals(40, level20.controlDc)
        assertEquals(
            "Ability boosts, envy of the world, Kingdom feat, ruin resistance",
            level20.kingdomFeatures,
        )
    }

    @Test
    fun midRowLevel10MatchesWorkbook() {
        val level10 = advancementData.first { it.level == 10 }
        assertEquals(27, level10.controlDc)
        assertEquals("Ability boosts, Kingdom feat, life of luxury", level10.kingdomFeatures)
    }

    @Test
    fun midRowLevel15MatchesWorkbook() {
        val level15 = advancementData.first { it.level == 15 }
        assertEquals(34, level15.controlDc)
        assertEquals(
            "Ability boosts, settlement construction (metropolis), skill increase",
            level15.kingdomFeatures,
        )
    }

    @Test
    fun allLevelsAreSequential() {
        advancementData.forEachIndexed { index, advancement ->
            assertEquals(index + 1, advancement.level, "Level at index $index should be ${index + 1}")
        }
    }

    @Test
    fun allControlDcsArePositive() {
        advancementData.forEach {
            assertTrue(it.controlDc > 0, "Control DC should be positive, got ${it.controlDc} at level ${it.level}")
        }
    }

    @Test
    fun controlDcsIncreaseWithLevel() {
        for (i in 1 until advancementData.size) {
            val prev = advancementData[i - 1]
            val curr = advancementData[i]
            assertTrue(
                curr.controlDc >= prev.controlDc,
                "Control DC should not decrease: level ${prev.level}=${prev.controlDc} -> level ${curr.level}=${curr.controlDc}",
            )
        }
    }

    @Test
    fun allKingdomFeaturesAreNonBlank() {
        advancementData.forEach {
            assertFalse(
                it.kingdomFeatures.isBlank(),
                "Kingdom features should not be blank at level ${it.level}",
            )
        }
    }

    @Test
    fun allKingdomFeaturesAreNonEmpty() {
        advancementData.forEach {
            assertTrue(
                it.kingdomFeatures.isNotEmpty(),
                "Kingdom features should not be empty at level ${it.level}",
            )
        }
    }

    @Test
    fun noDuplicateLevels() {
        val levels = advancementData.map { it.level }
        assertEquals(levels.size, levels.toSet().size, "All levels should be unique")
    }

    @Test
    fun levelsCoverFullRange() {
        val expected = (1..20).toList()
        assertEquals(expected, advancementData.map { it.level })
    }

    @Test
    fun findAdvancementReturnsCorrectRow() {
        val result = findAdvancement(5)
        assertNotNull(result, "findAdvancement(5) should not be null")
        assertEquals(5, result.level)
        assertEquals(20, result.controlDc)
        assertEquals("Ability boosts, ruin resistance, skill increase", result.kingdomFeatures)
    }

    @Test
    fun findAdvancementReturnsNullForInvalidLevel() {
        assertNull(findAdvancement(0), "Level 0 does not exist")
        assertNull(findAdvancement(21), "Level 21 does not exist")
        assertNull(findAdvancement(-1), "Negative level does not exist")
    }

    @Test
    fun findAdvancementCoversAllValidLevels() {
        (1..20).forEach { level ->
            val result = findAdvancement(level)
            assertNotNull(result, "findAdvancement($level) should not be null")
            assertEquals(level, result.level)
        }
    }

    @Test
    fun specificSpotCheckLevel8() {
        val level8 = advancementData.first { it.level == 8 }
        assertEquals(24, level8.controlDc)
        assertEquals("Experienced leadership +2, Kingdom feat, ruin resistance", level8.kingdomFeatures)
    }

    @Test
    fun specificSpotCheckLevel12() {
        val level12 = advancementData.first { it.level == 12 }
        assertEquals(30, level12.controlDc)
        assertEquals("Civic planning, Kingdom feat", level12.kingdomFeatures)
    }
}
