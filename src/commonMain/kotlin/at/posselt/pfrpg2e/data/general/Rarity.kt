package at.posselt.pfrpg2e.data.general

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class Rarity {
    COMMON,
    UNCOMMON,
    RARE,
    UNIQUE;

    companion object {
        fun fromString(value: String) = fromCamelCase<Rarity>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}