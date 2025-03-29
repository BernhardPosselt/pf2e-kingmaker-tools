package at.posselt.pfrpg2e.data.events

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class KingdomEventTrait {
    DOWNTIME,
    LEADERSHIP,
    HEX,
    DANGEROUS,
    BENEFICIAL,
    SETTLEMENT,
    CONTINUOUS;

    companion object {
        fun fromString(value: String) = fromCamelCase<KingdomEventTrait>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}