package at.posselt.pfrpg2e.data.kingdom.structures

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class StructureTrait {
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

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}