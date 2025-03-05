package at.posselt.pfrpg2e.data.kingdom

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class CommodityType {
    ORE,
    LUMBER,
    STONE,
    LUXURY,
    FOOD;

    companion object {
        fun fromString(value: String) = fromCamelCase<CommodityType>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}