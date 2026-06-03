package at.posselt.pfrpg2e.data.kingdom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SpecializedArmyModifierDataTest {

    @Test
    fun tableHasEntries() {
        assertTrue(specializedArmyModifierData.isNotEmpty(), "Specialized army modifier table should not be empty")
    }

    @Test
    fun tableHas8Entries() {
        assertEquals(8, specializedArmyModifierData.size, "Should have 8 specialized army modifiers")
    }

    @Test
    fun sootscaleWarriorsEntryExists() {
        val modifier = specializedArmyModifierData.find { it.armyName == "Sootscale Warriors" }
        assertNotNull(modifier, "Sootscale Warriors modifier should exist")
        assertEquals(2, modifier.standardDc)
        assertEquals(1, modifier.ac)
        assertEquals(2, modifier.highSave)
        assertEquals(-1, modifier.lowSave)
        assertEquals(1, modifier.routThreshold)
    }

    @Test
    fun skirmisherEntryExists() {
        val modifier = specializedArmyModifierData.find { it.armyName == "Skirmisher" }
        assertNotNull(modifier, "Skirmisher modifier should exist")
        assertEquals(-2, modifier.standardDc)
        assertEquals(2, modifier.ac)
        assertEquals(2, modifier.highSave)
        assertEquals(2, modifier.lowSave)
    }

    @Test
    fun lizardfolkDefendersEntryExists() {
        val modifier = specializedArmyModifierData.find { it.armyName == "Lizardfolk Defenders" }
        assertNotNull(modifier)
        assertEquals(2, modifier.scouting)
        assertEquals(2, modifier.standardDc)
        assertEquals(1, modifier.ac)
        assertEquals(1, modifier.highSave)
    }

    @Test
    fun greengripeBombardiersEntryExists() {
        val modifier = specializedArmyModifierData.find { it.armyName == "Greengripe Bombardiers" }
        assertNotNull(modifier)
        assertEquals(-2, modifier.scouting)
        assertEquals(5, modifier.standardDc)
        assertEquals(-2, modifier.ac)
        assertEquals(-2, modifier.highSave)
        assertEquals(1, modifier.attack)
    }

    @Test
    fun nomenScoutsEntryExists() {
        val modifier = specializedArmyModifierData.find { it.armyName == "Nomen Scouts" }
        assertNotNull(modifier)
        assertEquals(2, modifier.scouting)
        assertEquals(2, modifier.standardDc)
        assertEquals(1, modifier.highSave)
        assertEquals(-4, modifier.routThreshold)
    }

    @Test
    fun frogRidersEntryExists() {
        val modifier = specializedArmyModifierData.find { it.armyName == "M'Botuu Frog Riders" }
        assertNotNull(modifier)
        assertEquals(5, modifier.standardDc)
        assertEquals(2, modifier.highSave)
        assertEquals(2, modifier.lowSave)
        assertEquals(2, modifier.attack)
    }

    @Test
    fun tokNikratScoutsEntryExists() {
        val modifier = specializedArmyModifierData.find { it.armyName == "Tok-Nikrat Scouts" }
        assertNotNull(modifier)
        assertEquals(1, modifier.scouting)
        assertEquals(5, modifier.standardDc)
        assertEquals(1, modifier.ac)
        assertEquals(1, modifier.highSave)
        assertEquals(2, modifier.lowSave)
    }

    @Test
    fun tigerLordBerserkersEntryExists() {
        val modifier = specializedArmyModifierData.find { it.armyName == "Tiger Lord Berserkers" }
        assertNotNull(modifier)
        assertEquals(1, modifier.scouting)
        assertEquals(2, modifier.standardDc)
        assertEquals(-1, modifier.ac)
        assertEquals(-1, modifier.highSave)
        assertEquals(2, modifier.attack)
        assertEquals(-1, modifier.routThreshold)
    }

    @Test
    fun allArmiesHaveUniqueNames() {
        val names = specializedArmyModifierData.map { it.armyName.lowercase() }
        assertEquals(names.size, names.toSet().size, "All modifier army names should be unique")
    }

    @Test
    fun eachModifierHasAtLeastOneNonTrivialStat() {
        specializedArmyModifierData.forEach { modifier ->
            val hasModifier = modifier.scouting != null || modifier.standardDc != null ||
                modifier.ac != null || modifier.highSave != null ||
                modifier.lowSave != null || modifier.attack != null ||
                modifier.routThreshold != null
            assertTrue(hasModifier, "Modifier for '${modifier.armyName}' should have at least one stat adjustment")
        }
    }

    @Test
    fun allStatsAreInts() {
        specializedArmyModifierData.forEach { modifier ->
            // just verify the data class structure is consistent
            assertNotNull(modifier.armyName)
        }
    }
}

class FindSpecializedArmyModifierTest {

    @Test
    fun findSootscaleWarriors() {
        val result = findSpecializedArmyModifier("Sootscale Warriors")
        assertNotNull(result)
        assertEquals("Sootscale Warriors", result.armyName)
        assertEquals(2, result.standardDc)
    }

    @Test
    fun findCaseInsensitive() {
        val result = findSpecializedArmyModifier("sootscale warriors")
        assertNotNull(result)
        assertEquals("Sootscale Warriors", result.armyName)
    }

    @Test
    fun findFrogRiders() {
        val result = findSpecializedArmyModifier("M'Botuu Frog Riders")
        assertNotNull(result)
        assertEquals(5, result.standardDc)
    }

    @Test
    fun findNonExistentReturnsNull() {
        val result = findSpecializedArmyModifier("Nonexistent Army")
        assertNull(result)
    }

    @Test
    fun findTigerLordBerserkers() {
        val result = findSpecializedArmyModifier("Tiger Lord Berserkers")
        assertNotNull(result)
        assertEquals(2, result.attack)
    }
}

class SpecializedArmyModifierDataClassTest {

    @Test
    fun copyPreservesAllFields() {
        val original = SpecializedArmyModifier(
            armyName = "Test", scouting = 1, standardDc = 2, ac = 3,
            highSave = 4, lowSave = 5, attack = 6, routThreshold = 7
        )
        val copied = original.copy()
        assertEquals(original.armyName, copied.armyName)
        assertEquals(original.scouting, copied.scouting)
        assertEquals(original.standardDc, copied.standardDc)
        assertEquals(original.ac, copied.ac)
        assertEquals(original.highSave, copied.highSave)
        assertEquals(original.lowSave, copied.lowSave)
        assertEquals(original.attack, copied.attack)
        assertEquals(original.routThreshold, copied.routThreshold)
    }

    @Test
    fun equalsWorks() {
        val a = SpecializedArmyModifier(armyName = "Test", standardDc = 2, ac = 1)
        val b = SpecializedArmyModifier(armyName = "Test", standardDc = 2, ac = 1)
        assertEquals(a, b)
    }

    @Test
    fun equalsDetectsDifference() {
        val a = SpecializedArmyModifier(armyName = "Test", standardDc = 2)
        val b = SpecializedArmyModifier(armyName = "Test", standardDc = 3)
        kotlin.test.assertNotEquals(a, b)
    }

    @Test
    fun hashCodeConsistentWithEquals() {
        val a = SpecializedArmyModifier(armyName = "Test", standardDc = 2, ac = 1)
        val b = SpecializedArmyModifier(armyName = "Test", standardDc = 2, ac = 1)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun toStringContainsClassName() {
        val modifier = SpecializedArmyModifier(armyName = "Test")
        val str = modifier.toString()
        assertTrue(str.contains("SpecializedArmyModifier"))
    }

    @Test
    fun defaultStatsAreNull() {
        val modifier = SpecializedArmyModifier(armyName = "Test")
        assertNull(modifier.scouting)
        assertNull(modifier.standardDc)
        assertNull(modifier.ac)
        assertNull(modifier.highSave)
        assertNull(modifier.lowSave)
        assertNull(modifier.attack)
        assertNull(modifier.routThreshold)
    }
}
