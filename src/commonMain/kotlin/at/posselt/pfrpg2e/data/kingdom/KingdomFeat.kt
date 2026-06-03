package at.posselt.pfrpg2e.data.kingdom

/**
 * Kingdom feat data from the Tables sheet "Kingdom Feats" section.
 * Each feat has a name, level, optional prerequisite, and benefit text.
 */
data class KingdomFeat(
	val name: String,
	val level: Int,
	val prerequisite: String? = null,
	val benefit: String,
)

val kingdomFeats = listOf(
	KingdomFeat(
		name = "Civil Service",
		level = 1,
		benefit = "Everyone has a place and a role, and as long as those roles are filled, the government functions. When you select this feat, choose one leadership role; that role is now supported by your efficient civil servants, so its vacancy penalty is no longer applicable.\n\nYou gain a +2 status bonus to New Leadership checks.",
	),
	KingdomFeat(
		name = "Cooperative Leadership",
		level = 1,
		benefit = "Your leaders are skilled at working with one another. When a leader uses the Focused Attention kingdom activity to aid another leader's kingdom check, the circumstance bonus granted by a success is increased to +3.\n\nAt 11th level, when a leader uses the Focused Attention kingdom activity to aid another leader's check, treat a critical failure on the aided check as a failure. If your kingdom has at least the expert rank in the skill used in the aided check, treat a failure on the check as a success.",
	),
	KingdomFeat(
		name = "Fortified Fiefs",
		level = 1,
		prerequisite = "trained in Defense",
		benefit = "You gain a +2 circumstance bonus to checks attempted as part of the Fortify Hex activity and on activities to build or repair a Barracks, Castle, Garrison, Keep, Stone Wall, or Wooden Wall. You gain a +1 status bonus to all kingdom checks attempted during dangerous events that directly impact your settlements' defenses.",
	),
	KingdomFeat(
		name = "Insider Trading",
		level = 1,
		prerequisite = "trained in Industry",
		benefit = "You gain a +1 status bonus to Establish Work Site, Establish Trade Agreement, and Trade Commodities activities. In addition, gain 1 bonus Resource Die at the start of each Kingdom turn.",
	),
	KingdomFeat(
		name = "Kingdom Assurance",
		level = 1,
		prerequisite = "trained in at least three skills",
		benefit = "Choose one Kingdom skill in which your kingdom is trained. Once per Kingdom turn, when you would attempt a skill check for that skill, you can forgo rolling and instead take a result equal to 10 + your proficiency bonus. Special: You can select this feat multiple times, choosing a different skill each time.",
	),
	KingdomFeat(
		name = "Muddle Through",
		level = 1,
		prerequisite = "trained in Wilderness",
		benefit = "Your people are independent-minded and take care of the small things around the kingdom. Increase two of your Ruin thresholds by 1 and one of them by 2.",
	),
	KingdomFeat(
		name = "Practical Magic",
		level = 1,
		prerequisite = "trained in Magic",
		benefit = "You gain a +1 status bonus to Magic checks, and you can use Magic checks in place of Engineering checks. You reduce the cost of using the Hire Adventurers activity to 1 RP.",
	),
	KingdomFeat(
		name = "Pull Together",
		level = 1,
		prerequisite = "trained in Politics",
		benefit = "Once per Kingdom turn when you roll a critical failure on a Kingdom skill check, attempt a DC 11 flat check. If this succeeds, treat the result as failure instead.",
	),
	KingdomFeat(
		name = "Skill Training",
		level = 1,
		benefit = "Your kingdom receives the trained proficiency rank in a Kingdom skill of your choice. You can select this feat multiple times, choosing a new skill each time.",
	),
	KingdomFeat(
		name = "Crush Dissent",
		level = 3,
		prerequisite = "trained in Warfare",
		benefit = "Once per Kingdom turn when you gain Unrest, you can attempt to crush the dissent by attempting a basic Warfare check. On a success, the Unrest increase is canceled. You gain a +1 status bonus to checks to resolve dangerous kingdom events that involve internal bickering.",
	),
	KingdomFeat(
		name = "Inspiring Entertainment",
		level = 3,
		prerequisite = "Culture 14",
		benefit = "When you check for Unrest during the Upkeep phase of a Kingdom turn, you may roll a Culture-based check rather than a Loyalty-based check. Your kingdom gains a +2 status bonus to all Culture-based skill checks whenever your kingdom has at least 1 Unrest.",
	),
	KingdomFeat(
		name = "Liquidate Resources",
		level = 3,
		prerequisite = "Economy 14",
		benefit = "The first time during a Kingdom turn in which you are forced to spend RP as the result of a failed skill check or a dangerous event, and that expense reduces you to 0 RP, you may instead reduce your RP to 1 and treat the expense as if paid. At the start of your next Kingdom turn, roll 4 fewer Resource Dice.",
	),
	KingdomFeat(
		name = "Quick Recovery",
		level = 3,
		prerequisite = "Stability 14",
		benefit = "Whenever you attempt a skill check to end an ongoing harmful kingdom event, you gain a +4 status bonus to the check.",
	),
	KingdomFeat(
		name = "Free and Fair",
		level = 7,
		benefit = "You gain a +2 circumstance bonus to Loyalty-based checks attempted as part of the New Leadership and Pledge of Fealty activities. If you fail or critically fail such a check, you can spend 2 RP to reroll the check (without the +2 circumstance bonus).",
	),
	KingdomFeat(
		name = "Quality of Life",
		level = 7,
		benefit = "The first time you gain Luxury Commodities in a Kingdom turn, increase the total gained by 1. All of your settlements are treated as 1 level higher for the purposes of determining magic items offered for sale.",
	),
	KingdomFeat(
		name = "Fame and Fortune",
		level = 11,
		benefit = "Whenever you achieve a critical success on any Kingdom skill check during the Activity phase of a Kingdom turn, gain 1 bonus Resource Die at the start of your next Kingdom turn.",
	),
)

fun findKingdomFeat(name: String): KingdomFeat? =
	kingdomFeats.find { it.name.equals(name, ignoreCase = true) }
