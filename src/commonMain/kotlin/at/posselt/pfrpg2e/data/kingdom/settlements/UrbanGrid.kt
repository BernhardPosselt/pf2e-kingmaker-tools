package at.posselt.pfrpg2e.data.kingdom.settlements

/**
 * Represents the per-edge terrain toggles from the Urban Grid Template sheet (GAP-4).
 * Each cardinal direction can independently have water, bridge, wood wall, or stone wall.
 */
data class UrbanGridEdge(
	val hasWater: Boolean = false,
	val hasBridge: Boolean = false,
	val hasWoodWall: Boolean = false,
	val hasStoneWall: Boolean = false,
)

/**
 * Represents the four edges of a settlement's urban grid (North, East, South, West).
 * Maps to rows 31-33 of the Urban Grid Template sheet.
 */
data class SettlementEdges(
	val north: UrbanGridEdge = UrbanGridEdge(),
	val east: UrbanGridEdge = UrbanGridEdge(),
	val south: UrbanGridEdge = UrbanGridEdge(),
	val west: UrbanGridEdge = UrbanGridEdge(),
) {
	val waterBorders: Int
		get() = listOf(north, east, south, west).count { it.hasWater }

	val hasBridge: Boolean
		get() = listOf(north, east, south, west).any { it.hasBridge }

	val hasWoodWall: Boolean
		get() = listOf(north, east, south, west).any { it.hasWoodWall }

	val hasStoneWall: Boolean
		get() = listOf(north, east, south, west).any { it.hasStoneWall }
}

/**
 * Terrain type for a single lot cell within the 3x3 urban grid.
 * Maps to the block terrain labels in rows 10-28 of the Urban Grid Template sheet.
 */
enum class BlockTerrain {
	LAND,
	UNPAVED,
	PAVED,
	WATER,
	BRIDGE,
	WOOD_WALL,
	STONE_WALL;
}

/**
 * Represents terrain data for a single block (one of A-I) in the 3x3 grid.
 * Each block has 4 lot positions: top-left, top-right, bottom-left, bottom-right.
 * Derived from the edge toggles and infrastructure flags (GAP-2).
 */
data class BlockGrid(
	val topLeft: BlockTerrain = BlockTerrain.LAND,
	val topRight: BlockTerrain = BlockTerrain.LAND,
	val bottomLeft: BlockTerrain = BlockTerrain.LAND,
	val bottomRight: BlockTerrain = BlockTerrain.LAND,
) {
	val lots: List<BlockTerrain>
		get() = listOf(topLeft, topRight, bottomLeft, bottomRight)

	val waterCount: Int
		get() = lots.count { it == BlockTerrain.WATER }

	val bridgeCount: Int
		get() = lots.count { it == BlockTerrain.BRIDGE }

	val pavedCount: Int
		get() = lots.count { it == BlockTerrain.PAVED }

	val isLand: Boolean
		get() = lots.all { it == BlockTerrain.LAND }
}

/**
 * Represents the full 3x3 urban grid layout for a settlement.
 * Blocks are labeled A-I (row-major: A=top-left, C=top-right, G=bottom-left, I=bottom-right).
 * This is the structured data from the Urban Grid Template rows 10-28 (GAP-1, GAP-2).
 */
data class UrbanGrid(
	val blockA: BlockGrid = BlockGrid(),
	val blockB: BlockGrid = BlockGrid(),
	val blockC: BlockGrid = BlockGrid(),
	val blockD: BlockGrid = BlockGrid(),
	val blockE: BlockGrid = BlockGrid(),
	val blockF: BlockGrid = BlockGrid(),
	val blockG: BlockGrid = BlockGrid(),
	val blockH: BlockGrid = BlockGrid(),
	val blockI: BlockGrid = BlockGrid(),
) {
	val blocks: List<BlockGrid>
		get() = listOf(blockA, blockB, blockC, blockD, blockE, blockF, blockG, blockH, blockI)

	val totalWaterLots: Int
		get() = blocks.sumOf { it.waterCount }

	val totalBridgeLots: Int
		get() = blocks.sumOf { it.bridgeCount }

	val lotsBorderingWater: Int
		get() = totalWaterLots

	val occupiedBlocks: Int
		get() = blocks.count { !it.isLand }
}
