package at.posselt.pfrpg2e.data.kingdom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ArmyAttackTypeTest {

    @Test
    fun allAttackTypesExist() {
        val values = ArmyAttackType.entries
        assertTrue(values.contains(ArmyAttackType.MELEE))
        assertTrue(values.contains(ArmyAttackType.BOTH))
        assertTrue(values.contains(ArmyAttackType.RANGED))
    }

    @Test
    fun expectedNumberOfAttackTypes() {
        assertEquals(3, ArmyAttackType.entries.size)
    }
}

class ArmySaveBonusTest {

    @Test
    fun allSaveBonusTypesExist() {
        val values = ArmySaveBonus.entries
        assertTrue(values.contains(ArmySaveBonus.HIGH))
        assertTrue(values.contains(ArmySaveBonus.LOW))
    }

    @Test
    fun expectedNumberOfSaveBonusTypes() {
        assertEquals(2, ArmySaveBonus.entries.size)
    }
}

class ArmyTemplateDataTest {

    @Test
    fun tableHasEntries() {
        assertTrue(armyTemplateData.isNotEmpty(), "Army template table should not be empty")
    }

    @Test
    fun infantryIsAccessible() {
        val template = armyTemplateData.find { it.name == "Infantry" }
        assertNotNull(template, "Infantry template should exist")
        assertEquals(ArmyType.INFANTRY, template.type)
        assertTrue(template.accessible, "Infantry should be accessible")
        assertEquals(1, template.consumption)
        assertEquals(4, template.hp)
        assertEquals(1, template.minimumLevel)
        assertEquals(ArmyAttackType.MELEE, template.attackType)
        assertEquals(ArmySaveBonus.LOW, template.maneuverSave)
    }

    @Test
    fun cavalryIsAccessible() {
        val template = armyTemplateData.find { it.name == "Cavalry" }
        assertNotNull(template, "Cavalry template should exist")
        assertEquals(ArmyType.CAVALRY, template.type)
        assertTrue(template.accessible)
        assertEquals(2, template.consumption)
        assertEquals(3, template.minimumLevel)
        assertTrue(template.startingTactics.contains("Overrun"))
    }

    @Test
    fun siegeEnginesAreRanged() {
        val template = armyTemplateData.find { it.name == "Siege Engines" }
        assertNotNull(template)
        assertEquals(ArmyAttackType.RANGED, template.attackType)
        assertEquals(7, template.minimumLevel)
        assertTrue(template.startingTactics.contains("Engines of War"))
    }

    @Test
    fun uniqueArmiesAreNotAccessible() {
        val uniqueTemplates = armyTemplateData.filter { !it.accessible }
        assertTrue(uniqueTemplates.isNotEmpty())
        uniqueTemplates.forEach { template ->
            assertEquals(ArmyType.UNIQUE, template.type, "${template.name} should be UNIQUE type")
            assertNotNull(template.specialFaction, "${template.name} should have a special faction")
        }
    }

    @Test
    fun fourBasicArmiesExist() {
        val basicTemplates = armyTemplateData.filter { it.accessible }
        assertEquals(4, basicTemplates.size, "Should have 4 basic army templates (Infantry, Cavalry, Skirmishers, Siege Engines)")
    }

    @Test
    fun sootscaleWarriorsHaveCorrectStats() {
        val template = armyTemplateData.find { it.name == "Sootscale Warriors" }
        assertNotNull(template)
        assertEquals(ArmyType.UNIQUE, template.type)
        assertFalse(template.accessible)
        assertEquals("Sootscale Kobolds", template.specialFaction)
        assertEquals(ArmyAttackType.BOTH, template.attackType)
        assertTrue(template.startingTactics.contains("Accustomed to Panic"))
        assertTrue(template.startingTactics.contains("Darkvision"))
    }

    @Test
    fun tigerLordBerserkersAreLevel12() {
        val template = armyTemplateData.find { it.name == "Tiger Lord Berserkers" }
        assertNotNull(template)
        assertEquals(12, template.minimumLevel)
        assertEquals(6, template.hp)
        assertEquals(ArmyAttackType.MELEE, template.attackType)
        assertEquals(4, template.startingTactics.size)
    }

    @Test
    fun nomenScoutsHaveNegativeConsumption() {
        val template = armyTemplateData.find { it.name == "Nomen Scouts" }
        assertNotNull(template)
        assertEquals(-1, template.consumption, "Nomen Scouts should have negative consumption (reduce kingdom consumption)")
    }

    @Test
    fun frogRidersAreLevel10() {
        val template = armyTemplateData.find { it.name == "M'Botuu Frog Riders" }
        assertNotNull(template)
        assertEquals(10, template.minimumLevel)
        assertEquals(6, template.hp)
        assertEquals(2, template.consumption)
        assertEquals("M'Botuu", template.specialFaction)
    }

    @Test
    fun allTemplatesHaveDescriptions() {
        armyTemplateData.forEach { template ->
            assertTrue(template.description.isNotBlank(), "Template '${template.name}' should have a non-blank description")
        }
    }

    @Test
    fun allTemplatesHaveRangedAmmo() {
        armyTemplateData.forEach { template ->
            assertNotNull(template.rangedAmmo, "Template '${template.name}' should have ranged ammo value")
            assertTrue(template.rangedAmmo!! > 0, "Template '${template.name}' should have positive ranged ammo")
        }
    }

    @Test
    fun allTemplateNamesAreUnique() {
        val names = armyTemplateData.map { it.name.lowercase() }
        assertEquals(names.size, names.toSet().size, "All template names should be unique")
    }

    @Test
    fun uniqueArmiesHaveStartingTactics() {
        val uniqueTemplates = armyTemplateData.filter { !it.accessible }
        uniqueTemplates.forEach { template ->
            assertTrue(template.startingTactics.isNotEmpty(), "Unique army '${template.name}' should have starting tactics")
        }
    }
}

class FindArmyTemplateTest {

    @Test
    fun findInfantry() {
        val result = findArmyTemplate("Infantry")
        assertNotNull(result)
        assertEquals("Infantry", result.name)
        assertEquals(ArmyType.INFANTRY, result.type)
    }

    @Test
    fun findCavalry() {
        val result = findArmyTemplate("Cavalry")
        assertNotNull(result)
    }

    @Test
    fun findCaseInsensitive() {
        val result = findArmyTemplate("infantry")
        assertNotNull(result)
        assertEquals("Infantry", result.name)
    }

    @Test
    fun findSiegeEngines() {
        val result = findArmyTemplate("Siege Engines")
        assertNotNull(result)
    }

    @Test
    fun findNonExistentReturnsNull() {
        val result = findArmyTemplate("Nonexistent Army")
        assertNull(result)
    }

    @Test
    fun findTigerLordBerserkers() {
        val result = findArmyTemplate("Tiger Lord Berserkers")
        assertNotNull(result)
        assertEquals(12, result.minimumLevel)
    }
}

class ArmyTemplateDataClassTest {

    @Test
    fun copyPreservesAllFields() {
        val original = ArmyTemplate(
            name = "Test", type = ArmyType.INFANTRY, consumption = 1, hp = 4,
            minimumLevel = 1, attackType = ArmyAttackType.MELEE, rangedAmmo = 5,
            maneuverSave = ArmySaveBonus.LOW, specialFaction = null, accessible = true,
            startingTactics = listOf("Tactic1"), description = "Test desc",
        )
        val copied = original.copy()
        assertEquals(original.name, copied.name)
        assertEquals(original.type, copied.type)
        assertEquals(original.consumption, copied.consumption)
        assertEquals(original.hp, copied.hp)
        assertEquals(original.minimumLevel, copied.minimumLevel)
        assertEquals(original.attackType, copied.attackType)
        assertEquals(original.rangedAmmo, copied.rangedAmmo)
        assertEquals(original.maneuverSave, copied.maneuverSave)
        assertEquals(original.specialFaction, copied.specialFaction)
        assertEquals(original.accessible, copied.accessible)
        assertEquals(original.startingTactics, copied.startingTactics)
        assertEquals(original.description, copied.description)
    }

    @Test
    fun equalsWorks() {
        val a = ArmyTemplate(name = "T1", type = ArmyType.INFANTRY, consumption = 1, hp = 4, minimumLevel = 1,
            attackType = ArmyAttackType.MELEE, rangedAmmo = 5, maneuverSave = ArmySaveBonus.LOW,
            accessible = true, description = "d")
        val b = ArmyTemplate(name = "T1", type = ArmyType.INFANTRY, consumption = 1, hp = 4, minimumLevel = 1,
            attackType = ArmyAttackType.MELEE, rangedAmmo = 5, maneuverSave = ArmySaveBonus.LOW,
            accessible = true, description = "d")
        assertEquals(a, b)
    }

    @Test
    fun equalsDetectsDifference() {
        val a = ArmyTemplate(name = "T1", type = ArmyType.INFANTRY, consumption = 1, hp = 4, minimumLevel = 1,
            attackType = ArmyAttackType.MELEE, rangedAmmo = 5, maneuverSave = ArmySaveBonus.LOW,
            accessible = true, description = "d")
        val b = ArmyTemplate(name = "T2", type = ArmyType.CAVALRY, consumption = 2, hp = 4, minimumLevel = 3,
            attackType = ArmyAttackType.MELEE, rangedAmmo = 5, maneuverSave = ArmySaveBonus.HIGH,
            accessible = true, description = "d")
        kotlin.test.assertNotEquals(a, b)
    }

    @Test
    fun hashCodeConsistentWithEquals() {
        val a = ArmyTemplate(name = "T1", type = ArmyType.INFANTRY, consumption = 1, hp = 4, minimumLevel = 1,
            attackType = ArmyAttackType.MELEE, rangedAmmo = 5, maneuverSave = ArmySaveBonus.LOW,
            accessible = true, description = "d")
        val b = ArmyTemplate(name = "T1", type = ArmyType.INFANTRY, consumption = 1, hp = 4, minimumLevel = 1,
            attackType = ArmyAttackType.MELEE, rangedAmmo = 5, maneuverSave = ArmySaveBonus.LOW,
            accessible = true, description = "d")
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun toStringContainsClassName() {
        val template = ArmyTemplate(name = "Test", type = ArmyType.INFANTRY, consumption = 1, hp = 4, minimumLevel = 1,
            attackType = ArmyAttackType.MELEE, rangedAmmo = 5, maneuverSave = ArmySaveBonus.LOW,
            accessible = true, description = "d")
        val str = template.toString()
        assertTrue(str.contains("ArmyTemplate"))
    }

    @Test
    fun defaultSpecialFactionIsNull() {
        val template = ArmyTemplate(name = "Test", type = ArmyType.INFANTRY, consumption = 1, hp = 4, minimumLevel = 1,
            attackType = ArmyAttackType.MELEE, rangedAmmo = 5, maneuverSave = ArmySaveBonus.LOW,
            accessible = true, description = "d")
        assertNull(template.specialFaction)
    }

    @Test
    fun defaultStartingTacticsIsEmpty() {
        val template = ArmyTemplate(name = "Test", type = ArmyType.INFANTRY, consumption = 1, hp = 4, minimumLevel = 1,
            attackType = ArmyAttackType.MELEE, rangedAmmo = 5, maneuverSave = ArmySaveBonus.LOW,
            accessible = true, description = "d")
        assertTrue(template.startingTactics.isEmpty())
    }
}
