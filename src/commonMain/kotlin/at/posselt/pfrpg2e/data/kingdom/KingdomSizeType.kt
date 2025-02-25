package at.posselt.pfrpg2e.data.kingdom

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class KingdomSizeType {
    TERRITORY,
    PROVINCE,
    STATE,
    COUNTRY,
    DOMINION;

    companion object {
        fun fromString(value: String) = fromCamelCase<KingdomSizeType>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}