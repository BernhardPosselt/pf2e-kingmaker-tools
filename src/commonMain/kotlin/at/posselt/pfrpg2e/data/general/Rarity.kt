package at.posselt.pfrpg2e.data.general

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class Rarity : Translatable, ValueEnum {
    COMMON,
    UNCOMMON,
    RARE,
    UNIQUE;

    companion object {
        fun fromString(value: String) = fromCamelCase<Rarity>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "rarity.$value"
}