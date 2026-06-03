package at.posselt.pfrpg2e.data.kingdom

data class RpToXpRow(
	val levelLessThan: Int,
	val rate: Int,
	val sizeLessThan: Int,
	val xp: Int,
)

val rpToXpTable: List<RpToXpRow> = listOf(
	RpToXpRow(levelLessThan = 5, rate = 10, sizeLessThan = 10, xp = 100),
	RpToXpRow(levelLessThan = 9, rate = 7, sizeLessThan = 25, xp = 50),
	RpToXpRow(levelLessThan = 13, rate = 5, sizeLessThan = 50, xp = 25),
	RpToXpRow(levelLessThan = 17, rate = 2, sizeLessThan = 100, xp = 10),
	RpToXpRow(levelLessThan = 21, rate = 1, sizeLessThan = 1000, xp = 5),
)
