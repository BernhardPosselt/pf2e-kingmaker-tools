package at.posselt.pfrpg2e.data.kingdom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettlementTypeDataTest {

    @Test
    fun tableHasFourEntries() {
        assertEquals(4, settlementTypeData.size, "Settlement type table should have 4 entries (Village, Town, City, Metropolis)")
    }

    @Test
    fun villageIsFirst() {
        val village = settlementTypeData.first()
        assertEquals("Village", village.name)
        assertEquals(1, village.kingdomLevel)
        assertEquals(1, village.sizeBlocks)
        assertEquals(0, village.populationMin)
        assertEquals(400, village.populationMax)
    }

    @Test
    fun townEntryExists() {
        val town = settlementTypeData.find { it.name == "Town" }
        assertNotNull(town, "Town settlement type should exist")
        assertEquals(3, town.kingdomLevel)
        assertEquals(2, town.sizeBlocks)
        assertEquals(401, town.populationMin)
        assertEquals(2000, town.populationMax)
    }

    @Test
    fun cityEntryExists() {
        val city = settlementTypeData.find { it.name == "City" }
        assertNotNull(city, "City settlement type should exist")
        assertEquals(9, city.kingdomLevel)
        assertEquals(5, city.sizeBlocks)
        assertEquals(2001, city.populationMin)
        assertEquals(25000, city.populationMax)
    }

    @Test
    fun metropolisIsLast() {
        val metropolis = settlementTypeData.last()
        assertEquals("Metropolis", metropolis.name)
        assertEquals(15, metropolis.kingdomLevel)
        assertEquals(10, metropolis.sizeBlocks)
        assertEquals(25001, metropolis.populationMin)
        assertNull(metropolis.populationMax, "Metropolis should have no population max")
    }

    @Test
    fun namesAreInOrder() {
        val names = settlementTypeData.map { it.name }
        assertEquals(listOf("Village", "Town", "City", "Metropolis"), names)
    }

    @Test
    fun kingdomLevelsIncrease() {
        for (i in 1 until settlementTypeData.size) {
            assertTrue(
                settlementTypeData[i].kingdomLevel > settlementTypeData[i - 1].kingdomLevel,
                "Kingdom level should increase from ${settlementTypeData[i - 1].name} to ${settlementTypeData[i].name}"
            )
        }
    }

    @Test
    fun populationRangesAreContinuous() {
        for (i in 0 until settlementTypeData.size - 1) {
            val current = settlementTypeData[i]
            val next = settlementTypeData[i + 1]
            assertNotNull(current.populationMax)
            assertEquals(
                current.populationMax!! + 1, next.populationMin,
                "Population ranges should be continuous: ${current.name} max + 1 should equal ${next.name} min"
            )
        }
    }

    @Test
    fun sizeBlocksIncreaseWithLevel() {
        for (i in 1 until settlementTypeData.size) {
            assertTrue(
                settlementTypeData[i].sizeBlocks >= settlementTypeData[i - 1].sizeBlocks,
                "Size blocks should not decrease"
            )
        }
    }

    @Test
    fun allNamesAreUnique() {
        val names = settlementTypeData.map { it.name.lowercase() }
        assertEquals(names.size, names.toSet().size, "All settlement type names should be unique")
    }
}

class FindSettlementTypeTest {

    @Test
    fun findVillage() {
        val result = findSettlementType("Village")
        assertNotNull(result)
        assertEquals("Village", result.name)
    }

    @Test
    fun findTown() {
        val result = findSettlementType("Town")
        assertNotNull(result)
        assertEquals(3, result.kingdomLevel)
    }

    @Test
    fun findCity() {
        val result = findSettlementType("City")
        assertNotNull(result)
        assertEquals(9, result.kingdomLevel)
    }

    @Test
    fun findMetropolis() {
        val result = findSettlementType("Metropolis")
        assertNotNull(result)
        assertEquals(15, result.kingdomLevel)
        assertNull(result.populationMax)
    }

    @Test
    fun findCaseInsensitive() {
        val result = findSettlementType("village")
        assertNotNull(result)
        assertEquals("Village", result.name)
    }

    @Test
    fun findMixedCase() {
        val result = findSettlementType("cItY")
        assertNotNull(result)
        assertEquals("City", result.name)
    }

    @Test
    fun findNonExistentReturnsNull() {
        val result = findSettlementType("Hamlet")
        assertNull(result)
    }
}

class SettlementTypeDataClassTest {

    @Test
    fun copyPreservesAllFields() {
        val original = SettlementType(name = "Test", kingdomLevel = 1, sizeBlocks = 2, populationMin = 100, populationMax = 500)
        val copied = original.copy()
        assertEquals(original.name, copied.name)
        assertEquals(original.kingdomLevel, copied.kingdomLevel)
        assertEquals(original.sizeBlocks, copied.sizeBlocks)
        assertEquals(original.populationMin, copied.populationMin)
        assertEquals(original.populationMax, copied.populationMax)
    }

    @Test
    fun copyAllowsPopulationMaxOverride() {
        val original = SettlementType(name = "Test", kingdomLevel = 1, sizeBlocks = 1, populationMin = 0, populationMax = 100)
        val modified = original.copy(populationMax = null)
        assertNull(modified.populationMax)
        assertEquals(100, original.populationMax)
    }

    @Test
    fun equalsWorks() {
        val a = SettlementType(name = "Town", kingdomLevel = 3, sizeBlocks = 2, populationMin = 401, populationMax = 2000)
        val b = SettlementType(name = "Town", kingdomLevel = 3, sizeBlocks = 2, populationMin = 401, populationMax = 2000)
        assertEquals(a, b)
    }

    @Test
    fun equalsDetectsNullVsNonNullPopulationMax() {
        val a = SettlementType(name = "X", kingdomLevel = 1, sizeBlocks = 1, populationMin = 0, populationMax = 100)
        val b = SettlementType(name = "X", kingdomLevel = 1, sizeBlocks = 1, populationMin = 0, populationMax = null)
        kotlin.test.assertNotEquals(a, b)
    }

    @Test
    fun hashCodeConsistentWithEquals() {
        val a = SettlementType(name = "Town", kingdomLevel = 3, sizeBlocks = 2, populationMin = 401, populationMax = 2000)
        val b = SettlementType(name = "Town", kingdomLevel = 3, sizeBlocks = 2, populationMin = 401, populationMax = 2000)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun toStringContainsClassName() {
        val st = SettlementType(name = "Village", kingdomLevel = 1, sizeBlocks = 1, populationMin = 0, populationMax = 400)
        val str = st.toString()
        assertTrue(str.contains("SettlementType"))
    }
}
