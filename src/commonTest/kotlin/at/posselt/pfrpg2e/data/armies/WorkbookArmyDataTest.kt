package at.posselt.pfrpg2e.data.armies

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkbookArmyDataTest {
    @Test
    fun basicArmiesMatchWorkbookRows() {
        assertEquals(11, workbookBasicArmies.size)
        assertEquals(
            listOf(
                "Infantry",
                "Cavalry",
                "Sootscale Warriors",
                "Skirmishers",
                "Lizardfolk Defenders",
                "Siege Engines",
                "Greengripe Bombardiers",
                "Nomen Scouts",
                "M'Botuu Frog Riders",
                "Tok-Nikrat Scouts",
                "Tiger Lord Berserkers",
            ),
            workbookBasicArmies.map { it.name }
        )
    }

    @Test
    fun cavalryWorkbookArmyRowIsRepresented() {
        val cavalry = workbookBasicArmies.single { it.name == "Cavalry" }
        assertEquals(ArmyType.CAVALRY, cavalry.type)
        assertEquals(2, cavalry.consumption)
        assertEquals(4, cavalry.hitPoints)
        assertEquals(3, cavalry.minimumLevel)
        assertEquals(ArmyAttackMode.MELEE, cavalry.attacks)
        assertEquals(5, cavalry.rangedAmmo)
        assertEquals(ArmyManeuverSave.HIGH, cavalry.maneuverSave)
        assertTrue(cavalry.accessible)
        assertEquals(listOf("Overrun"), cavalry.startingTactics)
    }

    @Test
    fun sootscaleWorkbookArmyRowIsRepresented() {
        val sootscale = workbookBasicArmies.single { it.name == "Sootscale Warriors" }
        assertEquals(ArmyType.INFANTRY, sootscale.type)
        assertEquals("Sootscale Kobolds", sootscale.specialArmyFaction)
        assertFalse(sootscale.accessible)
        assertEquals(listOf("Accustomed to Panic", "Darkvision"), sootscale.startingTactics)
        assertEquals(ArmyAttackMode.BOTH, sootscale.attacks)
        assertEquals(ArmyManeuverSave.HIGH, sootscale.maneuverSave)
    }

    @Test
    fun armyTacticsMatchWorkbookRows() {
        assertEquals(40, workbookArmyTactics.size)
        assertEquals("Ambush", workbookArmyTactics.first().name)
        assertEquals("Warmongers", workbookArmyTactics.last().name)
    }

    @Test
    fun selectedArmyTacticsMatchWorkbookRows() {
        val ambush = workbookArmyTactics.single { it.name == "Ambush" }
        assertEquals(8, ambush.minimumLevel)
        assertEquals(listOf(ArmyType.SKIRMISHER), ambush.types)
        assertEquals(emptyList(), ambush.tacticalActions)

        val darkvision = workbookArmyTactics.single { it.name == "Darkvision" }
        assertEquals(
            listOf(ArmyType.CAVALRY, ArmyType.INFANTRY, ArmyType.SIEGE, ArmyType.SKIRMISHER),
            darkvision.types
        )

        val trample = workbookArmyTactics.single { it.name == "Trample" }
        assertEquals("Nomen Scouts", trample.uniqueArmy)
        assertEquals(listOf("Trample [three-action]"), trample.tacticalActions)
        assertEquals(listOf("Attack"), trample.actionTraits)

        val warmongers = workbookArmyTactics.single { it.name == "Warmongers" }
        assertEquals("Tiger Lord Berserkers", warmongers.uniqueArmy)
        assertEquals(
            listOf("All-Out Assault [two-action]", "Counterattack [reaction]", "Taunt [one-action]"),
            warmongers.tacticalActions
        )
        assertEquals(listOf("Attack", "Morale"), warmongers.actionTraits)
    }

    @Test
    fun specializedArmyModifiersMatchWorkbookRows() {
        assertEquals(8, workbookSpecializedArmyModifiers.size)

        val greengripe = workbookSpecializedArmyModifiers.single { it.name == "Greengripe Bombardiers" }
        assertEquals(-2, greengripe.scouting)
        assertEquals(5, greengripe.standardDc)
        assertEquals(-2, greengripe.ac)
        assertEquals(1, greengripe.attack)

        val skirmisher = workbookSpecializedArmyModifiers.single { it.name == "Skirmisher" }
        assertEquals(-2, skirmisher.ac)
        assertEquals(2, skirmisher.highSave)
        assertEquals(2, skirmisher.lowSave)
    }
}
