package at.posselt.pfrpg2e.data.kingdom

enum class ArmyType {
	CAVALRY,
	INFANTRY,
	SIEGE,
	SKIRMISHER,
	UNIQUE;
}

/**
 * Core army statistics per army level (1-20).
 * From the Tables sheet "Army Stats" section.
 */
data class ArmyStats(
	val level: Int,
	val scouting: Int,
	val standardDc: Int,
	val ac: Int,
	val highSave: Int,
	val lowSave: Int,
	val attack: Int,
	val maxTactics: Int,
)

val armyStatsData = listOf(
	ArmyStats(level = 1, scouting = 7, standardDc = 15, ac = 16, highSave = 10, lowSave = 4, attack = 9, maxTactics = 1),
	ArmyStats(level = 2, scouting = 8, standardDc = 16, ac = 18, highSave = 11, lowSave = 5, attack = 11, maxTactics = 1),
	ArmyStats(level = 3, scouting = 9, standardDc = 18, ac = 19, highSave = 12, lowSave = 6, attack = 12, maxTactics = 1),
	ArmyStats(level = 4, scouting = 11, standardDc = 19, ac = 21, highSave = 14, lowSave = 8, attack = 14, maxTactics = 2),
	ArmyStats(level = 5, scouting = 12, standardDc = 20, ac = 22, highSave = 15, lowSave = 9, attack = 15, maxTactics = 2),
	ArmyStats(level = 6, scouting = 14, standardDc = 22, ac = 24, highSave = 17, lowSave = 11, attack = 17, maxTactics = 2),
	ArmyStats(level = 7, scouting = 15, standardDc = 23, ac = 25, highSave = 18, lowSave = 12, attack = 18, maxTactics = 2),
	ArmyStats(level = 8, scouting = 16, standardDc = 24, ac = 27, highSave = 19, lowSave = 13, attack = 20, maxTactics = 3),
	ArmyStats(level = 9, scouting = 18, standardDc = 26, ac = 28, highSave = 21, lowSave = 15, attack = 21, maxTactics = 3),
	ArmyStats(level = 10, scouting = 19, standardDc = 27, ac = 30, highSave = 22, lowSave = 16, attack = 23, maxTactics = 3),
	ArmyStats(level = 11, scouting = 21, standardDc = 28, ac = 31, highSave = 24, lowSave = 18, attack = 24, maxTactics = 3),
	ArmyStats(level = 12, scouting = 22, standardDc = 30, ac = 33, highSave = 25, lowSave = 19, attack = 26, maxTactics = 4),
	ArmyStats(level = 13, scouting = 23, standardDc = 31, ac = 34, highSave = 26, lowSave = 21, attack = 27, maxTactics = 4),
	ArmyStats(level = 14, scouting = 25, standardDc = 32, ac = 36, highSave = 28, lowSave = 22, attack = 29, maxTactics = 4),
	ArmyStats(level = 15, scouting = 26, standardDc = 34, ac = 37, highSave = 29, lowSave = 24, attack = 30, maxTactics = 4),
	ArmyStats(level = 16, scouting = 28, standardDc = 35, ac = 39, highSave = 30, lowSave = 25, attack = 32, maxTactics = 5),
	ArmyStats(level = 17, scouting = 29, standardDc = 36, ac = 40, highSave = 32, lowSave = 26, attack = 33, maxTactics = 5),
	ArmyStats(level = 18, scouting = 30, standardDc = 38, ac = 42, highSave = 33, lowSave = 27, attack = 35, maxTactics = 5),
	ArmyStats(level = 19, scouting = 32, standardDc = 39, ac = 43, highSave = 35, lowSave = 29, attack = 36, maxTactics = 5),
	ArmyStats(level = 20, scouting = 33, standardDc = 40, ac = 45, highSave = 36, lowSave = 30, attack = 38, maxTactics = 6),
)

fun findArmyStats(level: Int): ArmyStats? =
	armyStatsData.find { it.level == level }
