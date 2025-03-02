package at.posselt.pfrpg2e.data.kingdom.structures

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class ItemGroup(val isMagical: Boolean) {
    DIVINE(true),
    PRIMAL(true),
    OCCULT(true),
    ARCANE(true),
    MAGICAL(true),
    ALCHEMICAL(false),
    LUXURY(false),
    OTHER(false);

    companion object {
        fun fromString(value: String) = fromCamelCase<ItemGroup>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}