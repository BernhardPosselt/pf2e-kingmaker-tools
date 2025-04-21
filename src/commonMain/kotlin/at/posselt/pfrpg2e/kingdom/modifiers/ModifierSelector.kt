package at.posselt.pfrpg2e.kingdom.modifiers

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class ModifierSelector : Translatable, ValueEnum {
    CHECK,
    ORE,
    STONE,
    LUMBER,
    CONSUMPTION;

    companion object {
        fun fromString(value: String) = fromCamelCase<ModifierSelector>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey = "modifierSelector.$value"
}