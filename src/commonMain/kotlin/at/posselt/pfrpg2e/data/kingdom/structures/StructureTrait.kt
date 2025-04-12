package at.posselt.pfrpg2e.data.kingdom.structures

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class StructureTrait: Translatable, ValueEnum {
    EDIFICE,
    YARD,
    BUILDING,
    FAMOUS,
    INFAMOUS,
    RESIDENTIAL,
    INFRASTRUCTURE;

    companion object {
        fun fromString(value: String) = fromCamelCase<StructureTrait>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "structureTrait.$value"
}