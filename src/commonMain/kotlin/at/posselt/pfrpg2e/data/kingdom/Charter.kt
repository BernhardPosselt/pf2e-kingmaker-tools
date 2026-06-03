package at.posselt.pfrpg2e.data.kingdom

data class Charter(
	val name: String,
	val boost: KingdomAbility?,
	val flaw: KingdomAbility?,
	val description: String,
)

val charters: List<Charter> = listOf(
	Charter(
		name = "Conquest",
		boost = KingdomAbility.LOYALTY,
		flaw = KingdomAbility.CULTURE,
		description = "Your sponsors have conquered an area and its former leaders have been routed or even killed. This charter places you in charge of some portion of this conquered territory (or land abandoned by the defeated enemy) and commands you to hold and pacify it in the name of your patron. The people are particularly devoted and supportive of your rule (if partially out of fear), but the constant threat of potential war hinders the arts and makes it difficult for citizens to truly relax. If you opt for this charter, you are asked to set up your kingdom against Pitax.",
	),
	Charter(
		name = "Expansion",
		boost = KingdomAbility.CULTURE,
		flaw = KingdomAbility.STABILITY,
		description = "Your patron places you in charge of a domain adjacent to already settled lands with the expectation that your nation will remain a strong ally. The greater support from your patron's nation helps to bolster your own kingdom's society, but this increased reliance means that fluctuations in your ally's fortunes can impede your own kingdom's security. If you select this charter, Lady Jamandi expects you to remain strong allies with Restov.",
	),
	Charter(
		name = "Exploration",
		boost = KingdomAbility.STABILITY,
		flaw = KingdomAbility.ECONOMY,
		description = "Your sponsor wants you to explore, clear, and settle a wilderness area along the border of the sponsor's own territory. Your charter helps to secure initial structures (or supplies to create them), at the cost of incurring financial debt.",
	),
	Charter(
		name = "Grant",
		boost = KingdomAbility.ECONOMY,
		flaw = KingdomAbility.LOYALTY,
		description = "Your patron grants a large amount of funding and other resources without restriction on the nature of your kingdom's development—but they do require you to employ many of their citizens and allies. Your nation's wealth and supplies are secure, but a portion of your kingdom's residents have split allegiances between your nation and that of your sponsor.",
	),
	Charter(
		name = "Open",
		boost = null,
		flaw = null,
		description = "If you would prefer to be truly free agents and trailblazers staking your own claim, you can simply choose an open charter with no restrictions—and no direct support. In this case, Lady Jamandi applauds your bravery and self-confidence, but warns that establishing a kingdom is no small task. An open charter grants a single ability boost to any ability score, and the new nation has no built-in ability flaw.",
	),
)

fun findCharter(name: String): Charter? =
	charters.find { it.name.equals(name, ignoreCase = true) }
