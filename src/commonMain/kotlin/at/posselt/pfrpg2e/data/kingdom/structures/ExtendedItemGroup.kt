package at.posselt.pfrpg2e.data.kingdom.structures

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class ExtendedItemGroup(val isMagical: Boolean) {
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

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}