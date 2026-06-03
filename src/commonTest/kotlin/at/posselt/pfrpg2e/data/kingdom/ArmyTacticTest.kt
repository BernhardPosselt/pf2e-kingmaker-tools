package at.posselt.pfrpg2e.data.kingdom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ArmyTacticDataTest {

    @Test
    fun tableHasEntries() {
        assertTrue(armyTacticData.isNotEmpty(), "Army tactic table should not be empty")
    }

    @Test
    fun holdTheLineIsAvailable() {
        val tactic = armyTacticData.find { it.name == "Hold the Line" }
        assertNotNull(tactic, "Hold the Line tactic should exist")
        assertEquals(1, tactic.minimumLevel)
    }

    @Test
    fun ambushIsSkirmisherOnly() {
        val tactic = armyTacticData.find { it.name == "Ambush" }
        assertNotNull(tactic, "Ambush tactic should exist")
        assertEquals(8, tactic.minimumLevel)
        assertTrue(tactic.armyTypes.contains(ArmyType.SKIRMISHER))
        assertEquals(1, tactic.armyTypes.size, "Ambush should only be for Skirmishers")
    }

    @Test
    fun darkvisionAppliesToAllBasicTypes() {
        val tactic = armyTacticData.find { it.name == "Darkvision" }
        assertNotNull(tactic, "Darkvision tactic should exist")
        assertEquals(1, tactic.minimumLevel)
        assertTrue(tactic.armyTypes.contains(ArmyType.CAVALRY))
        assertTrue(tactic.armyTypes.contains(ArmyType.INFANTRY))
        assertTrue(tactic.armyTypes.contains(ArmyType.SIEGE))
        assertTrue(tactic.armyTypes.contains(ArmyType.SKIRMISHER))
        assertFalse(tactic.armyTypes.contains(ArmyType.UNIQUE))
    }

    @Test
    fun uniqueTacticsExist() {
        val uniqueTactics = armyTacticData.filter { it.armyTypes.contains(ArmyType.UNIQUE) }
        assertTrue(uniqueTactics.isNotEmpty(), "There should be unique army tactics")
    }

    @Test
    fun frogRidersTacticsExist() {
        val frogTactics = armyTacticData.filter { tactic ->
            tactic.armyTypes.contains(ArmyType.UNIQUE) && tactic.name.contains("Frog", ignoreCase = true) ||
            tactic.description.contains("Frog", ignoreCase = true) ||
            tactic.description.contains("M'Botuu", ignoreCase = true)
        }
        assertTrue(frogTactics.isNotEmpty(), "Frog Rider tactics should exist")
    }

    @Test
    fun defensiveTacticsHasGrantedActions() {
        val tactic = armyTacticData.find { it.name == "Defensive Tactics" }
        assertNotNull(tactic, "Defensive Tactics should exist")
        assertTrue(tactic.grantedActions.isNotEmpty(), "Defensive Tactics should grant actions")
        assertTrue(tactic.grantedActions.any { it.contains("Defensive Stance", ignoreCase = true) })
        assertNotNull(tactic.trait)
        assertEquals("Maneuver", tactic.trait)
    }

    @Test
    fun flexibleTacticsHasMultipleGrantedActions() {
        val tactic = armyTacticData.find { it.name == "Flexible Tactics" }
        assertNotNull(tactic)
        assertTrue(tactic.grantedActions.size >= 3, "Flexible Tactics should grant multiple actions")
    }

    @Test
    fun allTacticsHaveDescriptions() {
        armyTacticData.forEach { tactic ->
            assertTrue(tactic.description.isNotBlank(), "Tactic '${tactic.name}' should have a non-blank description")
        }
    }

    @Test
    fun allTacticsHaveMinimumLevel() {
        armyTacticData.forEach { tactic ->
            assertTrue(tactic.minimumLevel >= 1, "Tactic '${tactic.name}' minimum level should be >= 1")
        }
    }

    @Test
    fun allTacticsHaveAtLeastOneArmyType() {
        armyTacticData.forEach { tactic ->
            assertTrue(tactic.armyTypes.isNotEmpty(), "Tactic '${tactic.name}' should have at least one army type")
        }
    }

    @Test
    fun minimumLevelsAreReasonable() {
        armyTacticData.forEach { tactic ->
            assertTrue(tactic.minimumLevel <= 20, "Tactic '${tactic.name}' minimum level should be <= 20")
        }
    }

    @Test
    fun enlargedAmmunitionIsFree() {
        val tactic = armyTacticData.find { it.name == "Increased Ammunition" }
        assertNotNull(tactic, "Increased Ammunition should exist")
        assertEquals(1, tactic.minimumLevel, "Increased Ammunition should be available at level 1")
    }

    @Test
    fun enginesOfWarIsSiegeOnly() {
        val tactic = armyTacticData.find { it.name == "Engines of War" }
        assertNotNull(tactic)
        assertEquals(7, tactic.minimumLevel)
        assertTrue(tactic.armyTypes.contains(ArmyType.SIEGE))
        assertEquals(1, tactic.armyTypes.size)
    }

    @Test
    fun bloodiedButUnbrokenAtLevel5() {
        val tactic = armyTacticData.find { it.name == "Bloodied, but Unbroken" }
        assertNotNull(tactic)
        assertEquals(5, tactic.minimumLevel)
        assertTrue(tactic.armyTypes.contains(ArmyType.CAVALRY))
        assertTrue(tactic.armyTypes.contains(ArmyType.INFANTRY))
        assertTrue(tactic.armyTypes.contains(ArmyType.SKIRMISHER))
    }
}

class FindArmyTacticTest {

    @Test
    fun findByNameCaseInsensitive() {
        val result = findArmyTactic("Ambush")
        assertNotNull(result)
        assertEquals("Ambush", result.name)
    }

    @Test
    fun findByNameLowerCase() {
        val result = findArmyTactic("ambush")
        assertNotNull(result)
        assertEquals("Ambush", result.name)
    }

    @Test
    fun findByNameMixedCase() {
        val result = findArmyTactic("hold the line")
        assertNotNull(result)
        assertEquals("Hold the Line", result.name)
    }

    @Test
    fun findNonExistentReturnsNull() {
        val result = findArmyTactic("Nonexistent Tactic")
        assertNull(result)
    }

    @Test
    fun findFlexibleTactics() {
        val result = findArmyTactic("Flexible Tactics")
        assertNotNull(result)
        assertEquals(5, result.minimumLevel)
    }

    @Test
    fun findFuriousCharge() {
        val result = findArmyTactic("Furious Charge")
        assertNotNull(result)
        assertEquals(10, result.minimumLevel)
    }
}

class ArmyTacticDataClassTest {

    @Test
    fun copyPreservesAllFields() {
        val original = ArmyTactic(
            name = "Test",
            minimumLevel = 1,
            armyTypes = listOf(ArmyType.INFANTRY),
            grantedActions = listOf("Test Action"),
            trait = "Maneuver",
            description = "Test description",
        )
        val copied = original.copy()
        assertEquals(original.name, copied.name)
        assertEquals(original.minimumLevel, copied.minimumLevel)
        assertEquals(original.armyTypes, copied.armyTypes)
        assertEquals(original.grantedActions, copied.grantedActions)
        assertEquals(original.trait, copied.trait)
        assertEquals(original.description, copied.description)
    }

    @Test
    fun equalsWorks() {
        val a = ArmyTactic(name = "T1", minimumLevel = 1, armyTypes = listOf(ArmyType.INFANTRY), description = "d")
        val b = ArmyTactic(name = "T1", minimumLevel = 1, armyTypes = listOf(ArmyType.INFANTRY), description = "d")
        assertEquals(a, b)
    }

    @Test
    fun equalsDetectsDifference() {
        val a = ArmyTactic(name = "T1", minimumLevel = 1, armyTypes = listOf(ArmyType.INFANTRY), description = "d")
        val b = ArmyTactic(name = "T2", minimumLevel = 2, armyTypes = listOf(ArmyType.CAVALRY), description = "d")
        kotlin.test.assertNotEquals(a, b)
    }

    @Test
    fun hashCodeConsistentWithEquals() {
        val a = ArmyTactic(name = "T1", minimumLevel = 1, armyTypes = listOf(ArmyType.INFANTRY), description = "d")
        val b = ArmyTactic(name = "T1", minimumLevel = 1, armyTypes = listOf(ArmyType.INFANTRY), description = "d")
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun toStringContainsClassName() {
        val tactic = ArmyTactic(name = "Test", minimumLevel = 1, armyTypes = listOf(ArmyType.INFANTRY), description = "d")
        val str = tactic.toString()
        assertTrue(str.contains("ArmyTactic"))
    }

    @Test
    fun defaultGrantedActionsIsEmpty() {
        val tactic = ArmyTactic(name = "Test", minimumLevel = 1, armyTypes = listOf(ArmyType.INFANTRY), description = "d")
        assertTrue(tactic.grantedActions.isEmpty())
    }

    @Test
    fun defaultTraitIsNull() {
        val tactic = ArmyTactic(name = "Test", minimumLevel = 1, armyTypes = listOf(ArmyType.INFANTRY), description = "d")
        assertNull(tactic.trait)
    }
}
