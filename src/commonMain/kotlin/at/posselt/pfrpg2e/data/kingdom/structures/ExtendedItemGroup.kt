package at.posselt.pfrpg2e.data.kingdom.structures

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class ExtendedItemGroup(val isMagical: Boolean): Translatable, ValueEnum {
    DIVINE(true),
    PRIMAL(true),
    OCCULT(true),
    ARCANE(true),
    DIVINE_LUXURY(true),
    PRIMAL_LUXURY(true),
    OCCULT_LUXURY(true),
    ARCANE_LUXURY(true),
    MAGICAL(true),
    ALCHEMICAL(false),
    LUXURY(false),
    OTHER(false);

    companion object {
        fun fromString(value: String) = fromCamelCase<ExtendedItemGroup>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "extendedItemGroup.$value"
}