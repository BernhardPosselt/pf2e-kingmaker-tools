package at.posselt.pfrpg2e.data.kingdom

data class Advancement(
	val level: Int,
	val controlDc: Int,
	val kingdomFeatures: String,
)

val advancementData: List<Advancement> = listOf(
	Advancement(
		level = 1,
		controlDc = 14,
		kingdomFeatures = "Charter, government, heartland, initial proficiencies, favored land, settlement construction (village)",
	),
	Advancement(
		level = 2,
		controlDc = 15,
		kingdomFeatures = "Kingdom feat",
	),
	Advancement(
		level = 3,
		controlDc = 16,
		kingdomFeatures = "Settlement construction (town), skill increase",
	),
	Advancement(
		level = 4,
		controlDc = 18,
		kingdomFeatures = "Expansion expert, fine living, Kingdom feat",
	),
	Advancement(
		level = 5,
		controlDc = 20,
		kingdomFeatures = "Ability boosts, ruin resistance, skill increase",
	),
	Advancement(
		level = 6,
		controlDc = 22,
		kingdomFeatures = "Kingdom feat",
	),
	Advancement(
		level = 7,
		controlDc = 23,
		kingdomFeatures = "Skill increase",
	),
	Advancement(
		level = 8,
		controlDc = 24,
		kingdomFeatures = "Experienced leadership +2, Kingdom feat, ruin resistance",
	),
	Advancement(
		level = 9,
		controlDc = 26,
		kingdomFeatures = "Expansion expert (Claim Hex 3 times/turn), settlement construction (city), skill increase",
	),
	Advancement(
		level = 10,
		controlDc = 27,
		kingdomFeatures = "Ability boosts, Kingdom feat, life of luxury",
	),
	Advancement(
		level = 11,
		controlDc = 28,
		kingdomFeatures = "Ruin resistance, skill increase",
	),
	Advancement(
		level = 12,
		controlDc = 30,
		kingdomFeatures = "Civic planning, Kingdom feat",
	),
	Advancement(
		level = 13,
		controlDc = 31,
		kingdomFeatures = "Skill increase",
	),
	Advancement(
		level = 14,
		controlDc = 32,
		kingdomFeatures = "Kingdom feat, ruin resistance",
	),
	Advancement(
		level = 15,
		controlDc = 34,
		kingdomFeatures = "Ability boosts, settlement construction (metropolis), skill increase",
	),
	Advancement(
		level = 16,
		controlDc = 35,
		kingdomFeatures = "Experienced leadership +3, Kingdom feat",
	),
	Advancement(
		level = 17,
		controlDc = 36,
		kingdomFeatures = "Ruin resistance, skill increase",
	),
	Advancement(
		level = 18,
		controlDc = 38,
		kingdomFeatures = "Kingdom feat",
	),
	Advancement(
		level = 19,
		controlDc = 39,
		kingdomFeatures = "Skill increase",
	),
	Advancement(
		level = 20,
		controlDc = 40,
		kingdomFeatures = "Ability boosts, envy of the world, Kingdom feat, ruin resistance",
	),
)

fun findAdvancement(level: Int): Advancement? =
	advancementData.find { it.level == level }
