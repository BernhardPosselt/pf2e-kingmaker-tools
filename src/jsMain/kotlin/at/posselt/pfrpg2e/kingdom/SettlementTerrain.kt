package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class SettlementTerrain {
    FOREST,
    SWAMP,
    MOUNTAINS,
    PLAINS;

    companion object {
        fun fromString(value: String) = fromCamelCase<SettlementTerrain>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}