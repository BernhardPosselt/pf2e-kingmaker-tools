package at.posselt.pfrpg2e.data.kingdom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WaterAdjacencyTest {

    @Test
    fun tableHasFourRows() {
        assertEquals(4, waterAdjacencyTable.size, "Water adjacency table should have exactly 4 directional rows")
    }

    @Test
    fun tableContainsAllFourDirections() {
        val faces = waterAdjacencyTable.map { it.face }
        assertTrue(faces.contains("East"), "Table should contain East")
        assertTrue(faces.contains("North"), "Table should contain North")
        assertTrue(faces.contains("South"), "Table should contain South")
        assertTrue(faces.contains("West"), "Table should contain West")
    }

    @Test
    fun noDuplicateFaces() {
        val faces = waterAdjacencyTable.map { it.face }
        assertEquals(faces.size, faces.toSet().size, "All faces should be unique")
    }

    @Test
    fun eastRowMatchesExpectedValues() {
        val east = waterAdjacencyTable.single { it.face == "East" }
        assertEquals("East", east.face)
        assertEquals("3,6,9", east.blockIds)
        assertEquals(2, east.lotId1)
        assertEquals(4, east.lotId2)
    }

    @Test
    fun northRowMatchesExpectedValues() {
        val north = waterAdjacencyTable.single { it.face == "North" }
        assertEquals("North", north.face)
        assertEquals("1,2,3", north.blockIds)
        assertEquals(1, north.lotId1)
        assertEquals(2, north.lotId2)
    }

    @Test
    fun southRowMatchesExpectedValues() {
        val south = waterAdjacencyTable.single { it.face == "South" }
        assertEquals("South", south.face)
        assertEquals("7,8,9", south.blockIds)
        assertEquals(3, south.lotId1)
        assertEquals(4, south.lotId2)
    }

    @Test
    fun westRowMatchesExpectedValues() {
        val west = waterAdjacencyTable.single { it.face == "West" }
        assertEquals("West", west.face)
        assertEquals("1,4,7", west.blockIds)
        assertEquals(1, west.lotId1)
        assertEquals(3, west.lotId2)
    }

    @Test
    fun allFacesAreNonBlank() {
        waterAdjacencyTable.forEach {
            assertFalse(it.face.isBlank(), "Face should not be blank")
        }
    }

    @Test
    fun allBlockIdsAreNonBlank() {
        waterAdjacencyTable.forEach {
            assertFalse(it.blockIds.isBlank(), "blockIds should not be blank for face ${it.face}")
        }
    }

    @Test
    fun allBlockIdsAreCommaSeparatedNumbers() {
        waterAdjacencyTable.forEach {
            val parts = it.blockIds.split(",")
            assertTrue(parts.isNotEmpty(), "blockIds should not be empty for face ${it.face}")
            parts.all { part ->
                part.trim().toIntOrNull() != null
            }.let { allNumeric ->
                assertTrue(allNumeric, "All blockIds should be numeric for face ${it.face}, got: ${it.blockIds}")
            }
        }
    }

    @Test
    fun allBlockIdEntriesHaveThreeBlocks() {
        waterAdjacencyTable.forEach {
            val blocks = it.blockIds.split(",")
            assertEquals(3, blocks.size, "Each face should reference exactly 3 block IDs, face ${it.face} has ${blocks.size}")
        }
    }

    @Test
    fun allLotIdsArePositive() {
        waterAdjacencyTable.forEach {
            assertTrue(it.lotId1 > 0, "lotId1 should be positive for face ${it.face}, got ${it.lotId1}")
            assertTrue(it.lotId2 > 0, "lotId2 should be positive for face ${it.face}, got ${it.lotId2}")
        }
    }

    @Test
    fun lotId1AndLotId2DifferForEachFace() {
        waterAdjacencyTable.forEach {
            assertNotEquals(it.lotId1, it.lotId2, "lotId1 and lotId2 should differ for face ${it.face}")
        }
    }

    @Test
    fun allBlockIdsAreUniqueSets() {
        // Each face references a distinct set of block IDs (no overlap in the comma-separated values)
        val blockSets = waterAdjacencyTable.map { it.blockIds.split(",").map { s -> s.trim() }.toSet() }
        // North/South/East/West should have distinct block sets for this data
        // (block 1 appears in North and West, block 3 in North/East, etc. — they share
        // individual blocks but the full sets should differ)
        for (i in blockSets.indices) {
            for (j in (i + 1) until blockSets.size) {
                assertNotEquals(
                    blockSets[i], blockSets[j],
                    "Block ID sets should differ between face ${waterAdjacencyTable[i].face} and ${waterAdjacencyTable[j].face}"
                )
            }
        }
    }

    @Test
    fun copyPreservesAllFields() {
        val original = waterAdjacencyTable[0]
        val copied = original.copy()
        assertEquals(original.face, copied.face)
        assertEquals(original.blockIds, copied.blockIds)
        assertEquals(original.lotId1, copied.lotId1)
        assertEquals(original.lotId2, copied.lotId2)
    }

    @Test
    fun copyAllowsFieldOverride() {
        val original = waterAdjacencyTable[0]
        val modified = original.copy(face = "Northeast")
        assertEquals("Northeast", modified.face)
        assertEquals(original.blockIds, modified.blockIds)
        assertEquals(original.lotId1, modified.lotId1)
        assertEquals(original.lotId2, modified.lotId2)
    }

    @Test
    fun equalsWorks() {
        val a = WaterAdjacency(face = "East", blockIds = "3,6,9", lotId1 = 2, lotId2 = 4)
        val b = WaterAdjacency(face = "East", blockIds = "3,6,9", lotId1 = 2, lotId2 = 4)
        assertEquals(a, b)
    }

    @Test
    fun equalsDetectsFaceDifference() {
        val a = WaterAdjacency(face = "East", blockIds = "1,2,3", lotId1 = 1, lotId2 = 2)
        val b = WaterAdjacency(face = "West", blockIds = "1,2,3", lotId1 = 1, lotId2 = 2)
        assertNotEquals(a, b)
    }

    @Test
    fun equalsDetectsBlockIdsDifference() {
        val a = WaterAdjacency(face = "East", blockIds = "3,6,9", lotId1 = 2, lotId2 = 4)
        val b = WaterAdjacency(face = "East", blockIds = "1,4,7", lotId1 = 2, lotId2 = 4)
        assertNotEquals(a, b)
    }

    @Test
    fun equalsDetectsLotId1Difference() {
        val a = WaterAdjacency(face = "East", blockIds = "3,6,9", lotId1 = 2, lotId2 = 4)
        val b = WaterAdjacency(face = "East", blockIds = "3,6,9", lotId1 = 99, lotId2 = 4)
        assertNotEquals(a, b)
    }

    @Test
    fun equalsDetectsLotId2Difference() {
        val a = WaterAdjacency(face = "East", blockIds = "3,6,9", lotId1 = 2, lotId2 = 4)
        val b = WaterAdjacency(face = "East", blockIds = "3,6,9", lotId1 = 2, lotId2 = 99)
        assertNotEquals(a, b)
    }

    @Test
    fun hashCodeConsistentWithEquals() {
        val a = WaterAdjacency(face = "North", blockIds = "1,2,3", lotId1 = 1, lotId2 = 2)
        val b = WaterAdjacency(face = "North", blockIds = "1,2,3", lotId1 = 1, lotId2 = 2)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun toStringContainsClassName() {
        val entry = WaterAdjacency(face = "East", blockIds = "3,6,9", lotId1 = 2, lotId2 = 4)
        val str = entry.toString()
        assertTrue(str.contains("WaterAdjacency"), "toString should contain class name")
        assertTrue(str.contains("East"), "toString should contain face value")
        assertTrue(str.contains("3,6,9"), "toString should contain blockIds value")
    }

    @Test
    fun canConstructInstanceDirectly() {
        val wa = WaterAdjacency(face = "Test", blockIds = "10,20,30", lotId1 = 5, lotId2 = 6)
        assertNotNull(wa)
        assertEquals("Test", wa.face)
        assertEquals("10,20,30", wa.blockIds)
        assertEquals(5, wa.lotId1)
        assertEquals(6, wa.lotId2)
    }

    @Test
    fun canDestructureAllComponents() {
        val (face, blockIds, lotId1, lotId2) = waterAdjacencyTable[0]
        assertEquals(waterAdjacencyTable[0].face, face)
        assertEquals(waterAdjacencyTable[0].blockIds, blockIds)
        assertEquals(waterAdjacencyTable[0].lotId1, lotId1)
        assertEquals(waterAdjacencyTable[0].lotId2, lotId2)
    }

    @Test
    fun rowsMatchExpectedOrder() {
        // The table lists East, North, South, West in that order
        assertEquals("East", waterAdjacencyTable[0].face)
        assertEquals("North", waterAdjacencyTable[1].face)
        assertEquals("South", waterAdjacencyTable[2].face)
        assertEquals("West", waterAdjacencyTable[3].face)
    }

    @Test
    fun blockIdsCoverAllNineBlocks() {
        // Collect all individual block IDs across all faces
        val allBlocks = waterAdjacencyTable
            .flatMap { it.blockIds.split(",") }
            .map { it.trim() }
            .sorted()
        // Blocks 1-9 should all appear at least once
        val expectedBlocks = (1..9).map { it.toString() }
        expectedBlocks.forEach { block ->
            assertTrue(allBlocks.contains(block), "Block $block should appear in at least one face's blockIds")
        }
    }
}
