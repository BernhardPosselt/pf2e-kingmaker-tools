package at.posselt.pfrpg2e.data.kingdom

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class Ruin(val ability: KingdomAbility) {
    CORRUPTION(KingdomAbility.CULTURE),
    CRIME(KingdomAbility.ECONOMY),
    DECAY(KingdomAbility.STABILITY),
    STRIFE(KingdomAbility.LOYALTY);

    companion object {
        fun fromString(value: String) = fromCamelCase<Ruin>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}