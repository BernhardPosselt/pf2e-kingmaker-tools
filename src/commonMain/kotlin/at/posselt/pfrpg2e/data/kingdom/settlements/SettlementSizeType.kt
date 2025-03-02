package at.posselt.pfrpg2e.data.kingdom.settlements

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class SettlementSizeType {
    VILLAGE,
    TOWN,
    CITY,
    METROPOLIS;

    companion object {
        fun fromString(value: String) = fromCamelCase<SettlementSizeType>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}