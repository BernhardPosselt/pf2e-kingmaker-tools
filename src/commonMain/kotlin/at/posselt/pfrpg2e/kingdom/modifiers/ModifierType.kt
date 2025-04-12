package at.posselt.pfrpg2e.kingdom.modifiers

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class ModifierType : Translatable, ValueEnum {
    ABILITY,
    PROFICIENCY,
    ITEM,
    STATUS,
    CIRCUMSTANCE,
    LEADERSHIP,
    VACANCY,
    UNTYPED;

    companion object {
        fun fromString(value: String) = fromCamelCase<ModifierType>(value)
    }

    override val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()

    override val i18nKey = "modifierType.$value"
}