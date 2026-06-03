package at.posselt.pfrpg2e.data.kingdom

/**
 * Specialized army modifier data from the Tables sheet "Specialized Army Modifiers" section.
 * Each modifier adjusts core stats for a specific army template.
 */
data class SpecializedArmyModifier(
	val armyName: String,
	val scouting: Int? = null,
	val standardDc: Int? = null,
	val ac: Int? = null,
	val highSave: Int? = null,
	val lowSave: Int? = null,
	val attack: Int? = null,
	val routThreshold: Int? = null,
)

val specializedArmyModifierData = listOf(
	SpecializedArmyModifier(armyName = "Sootscale Warriors", scouting = null, standardDc = 2, ac = 1, highSave = 2, lowSave = -1, routThreshold = 1),
	SpecializedArmyModifier(armyName = "Skirmisher", standardDc = -2, ac = 2, highSave = 2, lowSave = 2),
	SpecializedArmyModifier(armyName = "Lizardfolk Defenders", scouting = 2, standardDc = 2, ac = 1, highSave = 1),
	SpecializedArmyModifier(armyName = "Greengripe Bombardiers", scouting = -2, standardDc = 5, ac = -2, highSave = -2, attack = 1),
	SpecializedArmyModifier(armyName = "Nomen Scouts", scouting = 2, standardDc = 2, highSave = 1, routThreshold = -4),
	SpecializedArmyModifier(armyName = "M'Botuu Frog Riders", standardDc = 5, highSave = 2, lowSave = 2, attack = 2),
	SpecializedArmyModifier(armyName = "Tok-Nikrat Scouts", scouting = 1, standardDc = 5, ac = 1, highSave = 1, lowSave = 2),
	SpecializedArmyModifier(armyName = "Tiger Lord Berserkers", scouting = 1, standardDc = 2, ac = -1, highSave = -1, attack = 2, routThreshold = -1),
)

fun findSpecializedArmyModifier(armyName: String): SpecializedArmyModifier? =
	specializedArmyModifierData.find { it.armyName.equals(armyName, ignoreCase = true) }
