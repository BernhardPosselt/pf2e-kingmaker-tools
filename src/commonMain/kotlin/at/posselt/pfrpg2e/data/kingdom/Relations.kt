package at.posselt.pfrpg2e.data.kingdom

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class Relations {
    NONE,
    DIPLOMATIC_RELATIONS,
    TRADE_AGREEMENT;

    companion object {
        fun fromString(value: String) = fromCamelCase<Relations>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}