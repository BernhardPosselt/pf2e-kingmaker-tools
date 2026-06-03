package at.posselt.pfrpg2e.data.kingdom

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

/**
 * Army template data from the Tables sheet "Basic Armies" section.
 */
enum class ArmyAttackType : Translatable, ValueEnum {
	MELEE,
	BOTH,
	RANGED;

	override val value: String
		get() = toCamelCase()

	override val i18nKey: String
		get() = "armyAttackType.$value"
}

enum class ArmySaveBonus : Translatable, ValueEnum {
	HIGH,
	LOW;

	override val value: String
		get() = toCamelCase()

	override val i18nKey: String
		get() = "armySaveBonus.$value"
}

data class ArmyTemplate(
	val name: String,
	val type: ArmyType,
	val consumption: Int,
	val hp: Int,
	val minimumLevel: Int,
	val attackType: ArmyAttackType,
	val rangedAmmo: Int? = null,
	val maneuverSave: ArmySaveBonus,
	val specialFaction: String? = null,
	val accessible: Boolean,
	val startingTactics: List<String> = emptyList(),
	val description: String,
)

val armyTemplateData = listOf(
	ArmyTemplate(
		name = "Infantry",
		type = ArmyType.INFANTRY,
		consumption = 1,
		hp = 4,
		minimumLevel = 1,
		attackType = ArmyAttackType.MELEE,
		rangedAmmo = 5,
		maneuverSave = ArmySaveBonus.LOW,
		accessible = true,
		description = "This is a platoon of armored soldiers armed with melee weapons.",
	),
	ArmyTemplate(
		name = "Cavalry",
		type = ArmyType.CAVALRY,
		consumption = 2,
		hp = 4,
		minimumLevel = 3,
		attackType = ArmyAttackType.MELEE,
		rangedAmmo = 5,
		maneuverSave = ArmySaveBonus.HIGH,
		accessible = true,
		startingTactics = listOf("Overrun"),
		description = "This unit consists of armored soldiers armed with melee weapons and mounted on horses.",
	),
	ArmyTemplate(
		name = "Skirmishers",
		type = ArmyType.SKIRMISHER,
		consumption = 1,
		hp = 4,
		minimumLevel = 5,
		attackType = ArmyAttackType.MELEE,
		rangedAmmo = 5,
		maneuverSave = ArmySaveBonus.HIGH,
		accessible = true,
		description = "Skirmishers are lightly armored, but their ability to move quickly and to focus on individual tactics rather than working as a unit make them more resilient in other ways.",
	),
	ArmyTemplate(
		name = "Siege Engines",
		type = ArmyType.SIEGE,
		consumption = 1,
		hp = 6,
		minimumLevel = 7,
		attackType = ArmyAttackType.RANGED,
		rangedAmmo = 5,
		maneuverSave = ArmySaveBonus.LOW,
		accessible = true,
		startingTactics = listOf("Engines of War"),
		description = "A siege engine army consists of several catapults, ballistae, trebuchets, or other mechanized engines of war.",
	),
	ArmyTemplate(
		name = "Sootscale Warriors",
		type = ArmyType.UNIQUE,
		consumption = 1,
		hp = 4,
		minimumLevel = 3,
		attackType = ArmyAttackType.BOTH,
		rangedAmmo = 7,
		maneuverSave = ArmySaveBonus.HIGH,
		specialFaction = "Sootscale Kobolds",
		accessible = false,
		startingTactics = listOf("Accustomed to Panic", "Darkvision"),
		description = "Sootscale kobolds fight with shortswords and crossbows, although they tend to do so warily and cautiously.",
	),
	ArmyTemplate(
		name = "Lizardfolk Defenders",
		type = ArmyType.UNIQUE,
		consumption = 1,
		hp = 4,
		minimumLevel = 5,
		attackType = ArmyAttackType.BOTH,
		rangedAmmo = 5,
		maneuverSave = ArmySaveBonus.LOW,
		specialFaction = "Candlemere Lizardfolk",
		accessible = false,
		startingTactics = listOf("Swamp Dwellers"),
		description = "These lizardfolk are from the settlement on the banks of Candlemere; they fight with flails and javelins.",
	),
	ArmyTemplate(
		name = "Greengripe Bombardiers",
		type = ArmyType.UNIQUE,
		consumption = 2,
		hp = 6,
		minimumLevel = 7,
		attackType = ArmyAttackType.BOTH,
		rangedAmmo = 5,
		maneuverSave = ArmySaveBonus.LOW,
		specialFaction = "Greengripe",
		accessible = false,
		startingTactics = listOf("Burning Weaponry", "Darkvision", "Explosive Defeat"),
		description = "Greengripe goblins have built a mobile platform outfitted with a catapult-like flinging arm that can throw flammable debris.",
	),
	ArmyTemplate(
		name = "Nomen Scouts",
		type = ArmyType.UNIQUE,
		consumption = -1,
		hp = 4,
		minimumLevel = 8,
		attackType = ArmyAttackType.BOTH,
		rangedAmmo = 5,
		maneuverSave = ArmySaveBonus.LOW,
		specialFaction = "Nomen Centaurs",
		accessible = false,
		startingTactics = listOf("Brave", "Darkvision", "Self-Sufficient", "Trample"),
		description = "This band of Nomen centaurs fight with spears and longbows.",
	),
	ArmyTemplate(
		name = "M'Botuu Frog Riders",
		type = ArmyType.UNIQUE,
		consumption = 2,
		hp = 6,
		minimumLevel = 10,
		attackType = ArmyAttackType.BOTH,
		rangedAmmo = 5,
		maneuverSave = ArmySaveBonus.HIGH,
		specialFaction = "M'Botuu",
		accessible = false,
		startingTactics = listOf("Amphibious", "Chorus of Croaks", "Swamp Charge"),
		description = "These lance-armed boggards from M'botuu and ride giant frogs trained for warfare into battle.",
	),
	ArmyTemplate(
		name = "Tok-Nikrat Scouts",
		type = ArmyType.UNIQUE,
		consumption = 1,
		hp = 4,
		minimumLevel = 10,
		attackType = ArmyAttackType.BOTH,
		rangedAmmo = 5,
		maneuverSave = ArmySaveBonus.HIGH,
		specialFaction = "Tok-Nikrat",
		accessible = false,
		startingTactics = listOf("Hurl Nets", "Water Retreat", "Water Stride"),
		description = "Capable of striding across water, these bog striders from the settlement of Tok-Nikrat fight with nets and spears.",
	),
	ArmyTemplate(
		name = "Tiger Lord Berserkers",
		type = ArmyType.UNIQUE,
		consumption = 1,
		hp = 6,
		minimumLevel = 12,
		attackType = ArmyAttackType.MELEE,
		rangedAmmo = 5,
		maneuverSave = ArmySaveBonus.LOW,
		specialFaction = "Tiger Lords",
		accessible = false,
		startingTactics = listOf("Furious Charge", "Reactive Rally", "Revel in Battle", "Warmongers"),
		description = "These Tiger Lord barbarians use rage in battle; they fight with greataxes.",
	),
)

fun findArmyTemplate(name: String): ArmyTemplate? =
	armyTemplateData.find { it.name.equals(name, ignoreCase = true) }
