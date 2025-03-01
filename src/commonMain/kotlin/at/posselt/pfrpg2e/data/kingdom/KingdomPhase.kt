package at.posselt.pfrpg2e.data.kingdom

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class KingdomPhase {
    ARMY,
    CIVIC,
    COMMERCE,
    EVENT,
    LEADERSHIP,
    REGION,
    UPKEEP;


    companion object {
        fun fromString(value: String) = fromCamelCase<KingdomPhase>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}
