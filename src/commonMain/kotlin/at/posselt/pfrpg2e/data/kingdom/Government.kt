package at.posselt.pfrpg2e.data.kingdom

data class Government(
	val name: String,
	val abilityBoosts: Set<KingdomAbility>,
	val skillProficiencies: Set<KingdomSkill>,
	val bonusFeat: String,
)

val governments: List<Government> = listOf(
	Government(
		name = "Despotism",
		abilityBoosts = setOf(KingdomAbility.STABILITY, KingdomAbility.ECONOMY),
		skillProficiencies = setOf(KingdomSkill.INTRIGUE, KingdomSkill.WARFARE),
		bonusFeat = "Crush Dissent",
	),
	Government(
		name = "Feudalism",
		abilityBoosts = setOf(KingdomAbility.STABILITY, KingdomAbility.CULTURE),
		skillProficiencies = setOf(KingdomSkill.DEFENSE, KingdomSkill.TRADE),
		bonusFeat = "Fortified Fiefs",
	),
	Government(
		name = "Oligarchy",
		abilityBoosts = setOf(KingdomAbility.LOYALTY, KingdomAbility.ECONOMY),
		skillProficiencies = setOf(KingdomSkill.ARTS, KingdomSkill.INDUSTRY),
		bonusFeat = "Insider Trading",
	),
	Government(
		name = "Republic",
		abilityBoosts = setOf(KingdomAbility.STABILITY, KingdomAbility.LOYALTY),
		skillProficiencies = setOf(KingdomSkill.ENGINEERING, KingdomSkill.POLITICS),
		bonusFeat = "Pull Together",
	),
	Government(
		name = "Thaumocracy",
		abilityBoosts = setOf(KingdomAbility.ECONOMY, KingdomAbility.CULTURE),
		skillProficiencies = setOf(KingdomSkill.FOLKLORE, KingdomSkill.MAGIC),
		bonusFeat = "Practical Magic",
	),
	Government(
		name = "Yeomanry",
		abilityBoosts = setOf(KingdomAbility.LOYALTY, KingdomAbility.CULTURE),
		skillProficiencies = setOf(KingdomSkill.AGRICULTURE, KingdomSkill.WILDERNESS),
		bonusFeat = "Muddle Through",
	),
)

fun findGovernment(name: String): Government? =
	governments.find { it.name.equals(name, ignoreCase = true) }
