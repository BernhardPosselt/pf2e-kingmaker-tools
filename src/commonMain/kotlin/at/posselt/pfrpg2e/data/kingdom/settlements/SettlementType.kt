package at.posselt.pfrpg2e.data.kingdom.settlements

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class SettlementType {
    SETTLEMENT,
    CAPITAL;

    companion object {
        fun fromString(value: String) = fromCamelCase<SettlementType>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}