package at.posselt.pfrpg2e.data.actor

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.unslugify


sealed interface Attribute {
    val value: String
    val label: String
        get() = value.unslugify()
    val lorePostfixValue: String
        get() = "$value-lore"

    companion object {
        fun fromString(value: String): Attribute {
            return when (value) {
                "perception" -> Perception
                else -> fromCamelCase<Skill>(value) ?: Lore(value)
            }
        }
    }
}

enum class Skill : Attribute {
    ACROBATICS,
    ARCANA,
    ATHLETICS,
    CRAFTING,
    DECEPTION,
    DIPLOMACY,
    INTIMIDATION,
    MEDICINE,
    NATURE,
    OCCULTISM,
    PERFORMANCE,
    RELIGION,
    SOCIETY,
    STEALTH,
    SURVIVAL,
    THIEVERY;

    override val value: String
        get() = toCamelCase()
}

data object Perception : Attribute {
    override val value = "perception"
}

class Lore(value: String) : Attribute {
    override val value: String = value.removeSuffix("-lore")

    override fun equals(other: Any?): Boolean {
        return other is Lore && value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}

enum class Proficiency {
    UNTRAINED,
    TRAINED,
    EXPERT,
    MASTER,
    LEGENDARY;
}