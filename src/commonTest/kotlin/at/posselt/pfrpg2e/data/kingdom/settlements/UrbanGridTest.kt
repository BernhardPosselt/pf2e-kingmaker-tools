package at.posselt.pfrpg2e.data.kingdom.settlements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UrbanGridEdgeTest {

	@Test
	fun defaultValuesAreAllFalse() {
		val edge = UrbanGridEdge()
		assertFalse(edge.hasWater)
		assertFalse(edge.hasBridge)
		assertFalse(edge.hasWoodWall)
		assertFalse(edge.hasStoneWall)
	}

	@Test
	fun canSetWater() {
		val edge = UrbanGridEdge(hasWater = true)
		assertTrue(edge.hasWater)
		assertFalse(edge.hasBridge)
		assertFalse(edge.hasWoodWall)
		assertFalse(edge.hasStoneWall)
	}

	@Test
	fun canSetBridge() {
		val edge = UrbanGridEdge(hasBridge = true)
		assertFalse(edge.hasWater)
		assertTrue(edge.hasBridge)
	}

	@Test
	fun canSetWoodWall() {
		val edge = UrbanGridEdge(hasWoodWall = true)
		assertTrue(edge.hasWoodWall)
	}

	@Test
	fun canSetStoneWall() {
		val edge = UrbanGridEdge(hasStoneWall = true)
		assertTrue(edge.hasStoneWall)
	}

	@Test
	fun canSetMultipleFlags() {
		val edge = UrbanGridEdge(hasWater = true, hasBridge = true)
		assertTrue(edge.hasWater)
		assertTrue(edge.hasBridge)
		assertFalse(edge.hasWoodWall)
		assertFalse(edge.hasStoneWall)
	}

	@Test
	fun allFlagsTrue() {
		val edge = UrbanGridEdge(hasWater = true, hasBridge = true, hasWoodWall = true, hasStoneWall = true)
		assertTrue(edge.hasWater)
		assertTrue(edge.hasBridge)
		assertTrue(edge.hasWoodWall)
		assertTrue(edge.hasStoneWall)
	}

	@Test
	fun copyPreservesFields() {
		val original = UrbanGridEdge(hasWater = true, hasBridge = true)
		val copied = original.copy()
		assertEquals(original.hasWater, copied.hasWater)
		assertEquals(original.hasBridge, copied.hasBridge)
		assertEquals(original.hasWoodWall, copied.hasWoodWall)
		assertEquals(original.hasStoneWall, copied.hasStoneWall)
	}

	@Test
	fun equalsWorks() {
		val a = UrbanGridEdge(hasWater = true, hasBridge = false)
		val b = UrbanGridEdge(hasWater = true, hasBridge = false)
		assertEquals(a, b)
	}

	@Test
	fun equalsDetectsWaterDifference() {
		val a = UrbanGridEdge(hasWater = true)
		val b = UrbanGridEdge(hasWater = false)
		assertNotEquals(a, b)
	}

	@Test
	fun equalsDetectsBridgeDifference() {
		val a = UrbanGridEdge(hasBridge = true)
		val b = UrbanGridEdge(hasBridge = false)
		assertNotEquals(a, b)
	}

	@Test
	fun hashCodeConsistentWithEquals() {
		val a = UrbanGridEdge(hasWater = true, hasBridge = true)
		val b = UrbanGridEdge(hasWater = true, hasBridge = true)
		assertEquals(a.hashCode(), b.hashCode())
	}

	@Test
	fun toStringContainsClassName() {
		val edge = UrbanGridEdge(hasWater = true)
		val str = edge.toString()
		assertTrue(str.contains("UrbanGridEdge"))
	}
}

class SettlementEdgesTest {

	@Test
	fun defaultsHaveAllEdgesFalse() {
		val edges = SettlementEdges()
		assertFalse(edges.north.hasWater)
		assertFalse(edges.east.hasWater)
		assertFalse(edges.south.hasWater)
		assertFalse(edges.west.hasWater)
	}

	@Test
	fun waterBordersZeroWhenNoWater() {
		val edges = SettlementEdges()
		assertEquals(0, edges.waterBorders)
	}

	@Test
	fun waterBordersCountsCorrectly() {
		val edges = SettlementEdges(
			north = UrbanGridEdge(hasWater = true),
			south = UrbanGridEdge(hasWater = true),
		)
		assertEquals(2, edges.waterBorders)
	}

	@Test
	fun waterBordersMaxFour() {
		val edges = SettlementEdges(
			north = UrbanGridEdge(hasWater = true),
			east = UrbanGridEdge(hasWater = true),
			south = UrbanGridEdge(hasWater = true),
			west = UrbanGridEdge(hasWater = true),
		)
		assertEquals(4, edges.waterBorders)
	}

	@Test
	fun hasBridgeFalseWhenNone() {
		val edges = SettlementEdges()
		assertFalse(edges.hasBridge)
	}

	@Test
	fun hasBridgeTrueWhenOneEdge() {
		val edges = SettlementEdges(
			west = UrbanGridEdge(hasBridge = true),
		)
		assertTrue(edges.hasBridge)
	}

	@Test
	fun hasWoodWallFalseWhenNone() {
		val edges = SettlementEdges()
		assertFalse(edges.hasWoodWall)
	}

	@Test
	fun hasWoodWallTrueWhenSet() {
		val edges = SettlementEdges(
			north = UrbanGridEdge(hasWoodWall = true),
		)
		assertTrue(edges.hasWoodWall)
	}

	@Test
	fun hasStoneWallFalseWhenNone() {
		val edges = SettlementEdges()
		assertFalse(edges.hasStoneWall)
	}

	@Test
	fun hasStoneWallTrueWhenSet() {
		val edges = SettlementEdges(
			east = UrbanGridEdge(hasStoneWall = true),
		)
		assertTrue(edges.hasStoneWall)
	}

	@Test
	fun copyPreservesAllEdges() {
		val original = SettlementEdges(
			north = UrbanGridEdge(hasWater = true),
			south = UrbanGridEdge(hasBridge = true),
		)
		val copied = original.copy()
		assertEquals(original.north, copied.north)
		assertEquals(original.east, copied.east)
		assertEquals(original.south, copied.south)
		assertEquals(original.west, copied.west)
	}

	@Test
	fun copyAllowsEdgeOverride() {
		val original = SettlementEdges()
		val modified = original.copy(north = UrbanGridEdge(hasWater = true))
		assertTrue(modified.north.hasWater)
		assertFalse(original.north.hasWater)
	}

	@Test
	fun equalsWorks() {
		val a = SettlementEdges(north = UrbanGridEdge(hasWater = true))
		val b = SettlementEdges(north = UrbanGridEdge(hasWater = true))
		assertEquals(a, b)
	}

	@Test
	fun equalsDetectsDifference() {
		val a = SettlementEdges(north = UrbanGridEdge(hasWater = true))
		val b = SettlementEdges()
		assertNotEquals(a, b)
	}

	@Test
	fun hashCodeConsistentWithEquals() {
		val a = SettlementEdges(east = UrbanGridEdge(hasBridge = true))
		val b = SettlementEdges(east = UrbanGridEdge(hasBridge = true))
		assertEquals(a.hashCode(), b.hashCode())
	}

	@Test
	fun toStringContainsClassName() {
		val edges = SettlementEdges()
		val str = edges.toString()
		assertTrue(str.contains("SettlementEdges"))
	}
}

class BlockTerrainTest {

	@Test
	fun allEnumValuesExist() {
		val values = BlockTerrain.entries
		assertTrue(values.contains(BlockTerrain.LAND))
		assertTrue(values.contains(BlockTerrain.UNPAVED))
		assertTrue(values.contains(BlockTerrain.PAVED))
		assertTrue(values.contains(BlockTerrain.WATER))
		assertTrue(values.contains(BlockTerrain.BRIDGE))
		assertTrue(values.contains(BlockTerrain.WOOD_WALL))
		assertTrue(values.contains(BlockTerrain.STONE_WALL))
	}

	@Test
	fun expectedNumberOfTerrainTypes() {
		assertEquals(7, BlockTerrain.entries.size)
	}
}

class BlockGridTest {

	@Test
	fun defaultsToAllLand() {
		val grid = BlockGrid()
		assertEquals(BlockTerrain.LAND, grid.topLeft)
		assertEquals(BlockTerrain.LAND, grid.topRight)
		assertEquals(BlockTerrain.LAND, grid.bottomLeft)
		assertEquals(BlockTerrain.LAND, grid.bottomRight)
	}

	@Test
	fun lotsReturnsAllFourCells() {
		val grid = BlockGrid(
			topLeft = BlockTerrain.WATER,
			topRight = BlockTerrain.BRIDGE,
			bottomLeft = BlockTerrain.LAND,
			bottomRight = BlockTerrain.PAVED,
		)
		val lots = grid.lots
		assertEquals(4, lots.size)
		assertEquals(BlockTerrain.WATER, lots[0])
		assertEquals(BlockTerrain.BRIDGE, lots[1])
		assertEquals(BlockTerrain.LAND, lots[2])
		assertEquals(BlockTerrain.PAVED, lots[3])
	}

	@Test
	fun waterCountAllLand() {
		val grid = BlockGrid()
		assertEquals(0, grid.waterCount)
	}

	@Test
	fun waterCountMixed() {
		val grid = BlockGrid(
			topLeft = BlockTerrain.WATER,
			topRight = BlockTerrain.WATER,
			bottomLeft = BlockTerrain.LAND,
			bottomRight = BlockTerrain.LAND,
		)
		assertEquals(2, grid.waterCount)
	}

	@Test
	fun bridgeCountMixed() {
		val grid = BlockGrid(
			topLeft = BlockTerrain.BRIDGE,
			topRight = BlockTerrain.LAND,
			bottomLeft = BlockTerrain.BRIDGE,
			bottomRight = BlockTerrain.BRIDGE,
		)
		assertEquals(3, grid.bridgeCount)
	}

	@Test
	fun pavedCountMixed() {
		val grid = BlockGrid(
			topLeft = BlockTerrain.PAVED,
			topRight = BlockTerrain.PAVED,
			bottomLeft = BlockTerrain.UNPAVED,
			bottomRight = BlockTerrain.LAND,
		)
		assertEquals(2, grid.pavedCount)
	}

	@Test
	fun isLandTrueWhenAllLand() {
		val grid = BlockGrid()
		assertTrue(grid.isLand)
	}

	@Test
	fun isLandFalseWhenMixed() {
		val grid = BlockGrid(topLeft = BlockTerrain.WATER)
		assertFalse(grid.isLand)
	}

	@Test
	fun isLandFalseWhenAllNonLand() {
		val grid = BlockGrid(
			topLeft = BlockTerrain.PAVED,
			topRight = BlockTerrain.PAVED,
			bottomLeft = BlockTerrain.PAVED,
			bottomRight = BlockTerrain.PAVED,
		)
		assertFalse(grid.isLand)
	}

	@Test
	fun copyPreservesAllFields() {
		val original = BlockGrid(
			topLeft = BlockTerrain.WATER,
			topRight = BlockTerrain.BRIDGE,
			bottomLeft = BlockTerrain.LAND,
			bottomRight = BlockTerrain.PAVED,
		)
		val copied = original.copy()
		assertEquals(original.topLeft, copied.topLeft)
		assertEquals(original.topRight, copied.topRight)
		assertEquals(original.bottomLeft, copied.bottomLeft)
		assertEquals(original.bottomRight, copied.bottomRight)
	}

	@Test
	fun equalsWorks() {
		val a = BlockGrid(topLeft = BlockTerrain.WATER)
		val b = BlockGrid(topLeft = BlockTerrain.WATER)
		assertEquals(a, b)
	}

	@Test
	fun equalsDetectsDifference() {
		val a = BlockGrid(topLeft = BlockTerrain.WATER)
		val b = BlockGrid()
		assertNotEquals(a, b)
	}

	@Test
	fun hashCodeConsistentWithEquals() {
		val a = BlockGrid(topLeft = BlockTerrain.WATER, bottomRight = BlockTerrain.BRIDGE)
		val b = BlockGrid(topLeft = BlockTerrain.WATER, bottomRight = BlockTerrain.BRIDGE)
		assertEquals(a.hashCode(), b.hashCode())
	}

	@Test
	fun toStringContainsClassName() {
		val grid = BlockGrid()
		val str = grid.toString()
		assertTrue(str.contains("BlockGrid"))
	}
}

class UrbanGridTest {

	@Test
	fun defaultsToNineLandBlocks() {
		val grid = UrbanGrid()
		assertEquals(9, grid.blocks.size)
		grid.blocks.forEach { block ->
			assertEquals(BlockTerrain.LAND, block.topLeft)
			assertEquals(BlockTerrain.LAND, block.topRight)
			assertEquals(BlockTerrain.LAND, block.bottomLeft)
			assertEquals(BlockTerrain.LAND, block.bottomRight)
		}
	}

	@Test
	fun blocksOrderedAiToI() {
		val grid = UrbanGrid(
			blockA = BlockGrid(topLeft = BlockTerrain.WATER),
			blockB = BlockGrid(topLeft = BlockTerrain.BRIDGE),
			blockC = BlockGrid(topLeft = BlockTerrain.PAVED),
			blockD = BlockGrid(topLeft = BlockTerrain.UNPAVED),
			blockE = BlockGrid(topLeft = BlockTerrain.WOOD_WALL),
			blockF = BlockGrid(topLeft = BlockTerrain.STONE_WALL),
			blockG = BlockGrid(topLeft = BlockTerrain.WATER),
			blockH = BlockGrid(topLeft = BlockTerrain.BRIDGE),
			blockI = BlockGrid(topLeft = BlockTerrain.PAVED),
		)
		assertEquals(BlockTerrain.WATER, grid.blocks[0].topLeft)     // A
		assertEquals(BlockTerrain.BRIDGE, grid.blocks[1].topLeft)     // B
		assertEquals(BlockTerrain.PAVED, grid.blocks[2].topLeft)       // C
		assertEquals(BlockTerrain.UNPAVED, grid.blocks[3].topLeft)     // D
		assertEquals(BlockTerrain.WOOD_WALL, grid.blocks[4].topLeft)   // E
		assertEquals(BlockTerrain.STONE_WALL, grid.blocks[5].topLeft) // F
		assertEquals(BlockTerrain.WATER, grid.blocks[6].topLeft)       // G
		assertEquals(BlockTerrain.BRIDGE, grid.blocks[7].topLeft)      // H
		assertEquals(BlockTerrain.PAVED, grid.blocks[8].topLeft)        // I
	}

	@Test
	fun totalWaterLotsSumsAcrossAllBlocks() {
		val grid = UrbanGrid(
			blockA = BlockGrid(topLeft = BlockTerrain.WATER, topRight = BlockTerrain.WATER),
			blockB = BlockGrid(topLeft = BlockTerrain.WATER),
		)
		assertEquals(3, grid.totalWaterLots)
	}

	@Test
	fun totalBridgeLotsSumsAcrossAllBlocks() {
		val grid = UrbanGrid(
			blockA = BlockGrid(topLeft = BlockTerrain.BRIDGE, bottomRight = BlockTerrain.BRIDGE),
			blockC = BlockGrid(bottomLeft = BlockTerrain.BRIDGE),
		)
		assertEquals(3, grid.totalBridgeLots)
	}

	@Test
	fun lotsBorderingWaterEqualsTotalWaterLots() {
		val grid = UrbanGrid(
			blockA = BlockGrid(topLeft = BlockTerrain.WATER, topRight = BlockTerrain.WATER),
			blockB = BlockGrid(bottomLeft = BlockTerrain.WATER),
		)
		assertEquals(grid.totalWaterLots, grid.lotsBorderingWater)
		assertEquals(3, grid.lotsBorderingWater)
	}

	@Test
	fun occupiedBlocksZeroWhenAllLand() {
		val grid = UrbanGrid()
		assertEquals(0, grid.occupiedBlocks)
	}

	@Test
	fun occupiedBlocksCountsNonLandBlocks() {
		val grid = UrbanGrid(
			blockA = BlockGrid(topLeft = BlockTerrain.WATER),
			blockB = BlockGrid(topLeft = BlockTerrain.PAVED),
			blockC = BlockGrid(topLeft = BlockTerrain.BRIDGE),
		)
		assertEquals(3, grid.occupiedBlocks)
	}

	@Test
	fun occupiedBlocksMaxNine() {
		val grid = UrbanGrid(
			blockA = BlockGrid(topLeft = BlockTerrain.WATER),
			blockB = BlockGrid(topLeft = BlockTerrain.WATER),
			blockC = BlockGrid(topLeft = BlockTerrain.WATER),
			blockD = BlockGrid(topLeft = BlockTerrain.WATER),
			blockE = BlockGrid(topLeft = BlockTerrain.WATER),
			blockF = BlockGrid(topLeft = BlockTerrain.WATER),
			blockG = BlockGrid(topLeft = BlockTerrain.WATER),
			blockH = BlockGrid(topLeft = BlockTerrain.WATER),
			blockI = BlockGrid(topLeft = BlockTerrain.WATER),
		)
		assertEquals(9, grid.occupiedBlocks)
	}

	@Test
	fun copyPreservesAllBlocks() {
		val original = UrbanGrid(
			blockA = BlockGrid(topLeft = BlockTerrain.WATER),
			blockE = BlockGrid(topLeft = BlockTerrain.BRIDGE),
		)
		val copied = original.copy()
		assertEquals(original.blockA, copied.blockA)
		assertEquals(original.blockE, copied.blockE)
	}

	@Test
	fun copyAllowsBlockOverride() {
		val original = UrbanGrid()
		val modified = original.copy(blockA = BlockGrid(topLeft = BlockTerrain.WATER))
		assertEquals(BlockTerrain.WATER, modified.blockA.topLeft)
		assertEquals(BlockTerrain.LAND, original.blockA.topLeft)
	}

	@Test
	fun equalsWorks() {
		val a = UrbanGrid(blockA = BlockGrid(topLeft = BlockTerrain.WATER))
		val b = UrbanGrid(blockA = BlockGrid(topLeft = BlockTerrain.WATER))
		assertEquals(a, b)
	}

	@Test
	fun equalsDetectsDifference() {
		val a = UrbanGrid(blockA = BlockGrid(topLeft = BlockTerrain.WATER))
		val b = UrbanGrid()
		assertNotEquals(a, b)
	}

	@Test
	fun hashCodeConsistentWithEquals() {
		val a = UrbanGrid(blockD = BlockGrid(bottomRight = BlockTerrain.BRIDGE))
		val b = UrbanGrid(blockD = BlockGrid(bottomRight = BlockTerrain.BRIDGE))
		assertEquals(a.hashCode(), b.hashCode())
	}

	@Test
	fun toStringContainsClassName() {
		val grid = UrbanGrid()
		val str = grid.toString()
		assertTrue(str.contains("UrbanGrid"))
	}
}
