package at.posselt.pfrpg2e.data.kingdom

/**
 * Months of the year from the Tables sheet / Golarion calendar.
 */
data class Month(
	val name: String,
	val order: Int,
)

val months = listOf(
	Month(name = "Abadius", order = 1),
	Month(name = "Calistril", order = 2),
	Month(name = "Pharast", order = 3),
	Month(name = "Gozran", order = 4),
	Month(name = "Desnus", order = 5),
	Month(name = "Sarenith", order = 6),
	Month(name = "Erastus", order = 7),
	Month(name = "Arodus", order = 8),
	Month(name = "Rova", order = 9),
	Month(name = "Lamashan", order = 10),
	Month(name = "Neth", order = 11),
	Month(name = "Kuthona", order = 12),
)

/**
 * Proficiency bonus values by rank from the Tables sheet "Proficiency Bonuses" section.
 */
data class ProficiencyBonus(
	val rank: String,
	val bonus: Int,
)

val proficiencyBonuses = listOf(
	ProficiencyBonus(rank = "Untrained", bonus = 0),
	ProficiencyBonus(rank = "Trained", bonus = 2),
	ProficiencyBonus(rank = "Expert", bonus = 4),
	ProficiencyBonus(rank = "Master", bonus = 6),
	ProficiencyBonus(rank = "Legendary", bonus = 8),
)
