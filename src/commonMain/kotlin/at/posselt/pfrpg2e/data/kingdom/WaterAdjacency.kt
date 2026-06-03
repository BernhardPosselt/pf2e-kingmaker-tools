package at.posselt.pfrpg2e.data.kingdom

data class WaterAdjacency(
	val face: String,
	val blockIds: String,
	val lotId1: Int,
	val lotId2: Int,
)

val waterAdjacencyTable: List<WaterAdjacency> = listOf(
	WaterAdjacency(face = "East", blockIds = "3,6,9", lotId1 = 2, lotId2 = 4),
	WaterAdjacency(face = "North", blockIds = "1,2,3", lotId1 = 1, lotId2 = 2),
	WaterAdjacency(face = "South", blockIds = "7,8,9", lotId1 = 3, lotId2 = 4),
	WaterAdjacency(face = "West", blockIds = "1,4,7", lotId1 = 1, lotId2 = 3),
)
