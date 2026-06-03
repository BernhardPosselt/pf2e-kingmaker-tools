package at.posselt.pfrpg2e.data.kingdom

/**
 * RP-to-XP conversion rate table from the Tables sheet.
 * Used in V&K mode where conversion rate depends on kingdom level and kingdom size.
 *
 * Rows: kingdom level thresholds (5, 9, 13, 17)
 * Columns: kingdom size thresholds (10, 25, 50, 100)
 */
data class RpToXpRate(
	val levelLessThan: Int,
	val sizeLessThan: Int,
	val rate: Int,
)

val rpToXpRateData = listOf(
	// Level < 5
	RpToXpRate(levelLessThan = 5, sizeLessThan = 10, rate = 100),
	RpToXpRate(levelLessThan = 5, sizeLessThan = 25, rate = 100),
	RpToXpRate(levelLessThan = 5, sizeLessThan = 50, rate = 100),
	RpToXpRate(levelLessThan = 5, sizeLessThan = 100, rate = 100),
	// Level < 9
	RpToXpRate(levelLessThan = 9, sizeLessThan = 10, rate = 10),
	RpToXpRate(levelLessThan = 9, sizeLessThan = 25, rate = 10),
	RpToXpRate(levelLessThan = 9, sizeLessThan = 50, rate = 10),
	RpToXpRate(levelLessThan = 9, sizeLessThan = 100, rate = 10),
	// Level < 13
	RpToXpRate(levelLessThan = 13, sizeLessThan = 10, rate = 7),
	RpToXpRate(levelLessThan = 13, sizeLessThan = 25, rate = 7),
	RpToXpRate(levelLessThan = 13, sizeLessThan = 50, rate = 5),
	RpToXpRate(levelLessThan = 13, sizeLessThan = 100, rate = 5),
	// Level < 17
	RpToXpRate(levelLessThan = 17, sizeLessThan = 10, rate = 5),
	RpToXpRate(levelLessThan = 17, sizeLessThan = 25, rate = 2),
	RpToXpRate(levelLessThan = 17, sizeLessThan = 50, rate = 2),
	RpToXpRate(levelLessThan = 17, sizeLessThan = 100, rate = 1),
	// Level >= 17
	RpToXpRate(levelLessThan = Int.MAX_VALUE, sizeLessThan = 10, rate = 2),
	RpToXpRate(levelLessThan = Int.MAX_VALUE, sizeLessThan = 25, rate = 1),
	RpToXpRate(levelLessThan = Int.MAX_VALUE, sizeLessThan = 50, rate = 1),
	RpToXpRate(levelLessThan = Int.MAX_VALUE, sizeLessThan = 100, rate = 5),
)

/**
 * Random Event XP table from the Tables sheet.
 * Maps event level modifier to XP award.
 */
data class RandomEventXp(
	val eventLevel: Int,
	val xp: Int,
)

val randomEventXpData = listOf(
	RandomEventXp(eventLevel = -1, xp = 30),
	RandomEventXp(eventLevel = 0, xp = 40),
	RandomEventXp(eventLevel = 1, xp = 60),
	RandomEventXp(eventLevel = 2, xp = 80),
	RandomEventXp(eventLevel = 3, xp = 120),
)

/**
 * Hex Claimed XP table from the Tables sheet.
 * Maps kingdom size to XP per hex claimed.
 */
data class HexClaimedXp(
	val sizeLessThan: Int,
	val xp: Int,
)

val hexClaimedXpData = listOf(
	HexClaimedXp(sizeLessThan = 10, xp = 100),
	HexClaimedXp(sizeLessThan = 25, xp = 50),
	HexClaimedXp(sizeLessThan = 50, xp = 25),
	HexClaimedXp(sizeLessThan = 100, xp = 10),
	HexClaimedXp(sizeLessThan = Int.MAX_VALUE, xp = 5),
)
