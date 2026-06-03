package at.posselt.pfrpg2e.data.kingdom

data class Heartland(
	val terrain: String,
	val abilityBoost: KingdomAbility,
)

val heartlands: List<Heartland> = listOf(
	Heartland(
		terrain = "Forest or Swamp",
		abilityBoost = KingdomAbility.CULTURE,
	),
	Heartland(
		terrain = "Hill or Plain",
		abilityBoost = KingdomAbility.LOYALTY,
	),
	Heartland(
		terrain = "Lake or River",
		abilityBoost = KingdomAbility.ECONOMY,
	),
	Heartland(
		terrain = "Mountain or Ruins",
		abilityBoost = KingdomAbility.STABILITY,
	),
)
