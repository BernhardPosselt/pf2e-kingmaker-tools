package at.posselt.pfrpg2e.data.kingdom.leaders

import at.posselt.pfrpg2e.data.kingdom.KingdomAbility
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class Leader(val keyAbility: KingdomAbility) {
    RULER(KingdomAbility.LOYALTY),
    COUNSELOR(KingdomAbility.CULTURE),
    EMISSARY(KingdomAbility.LOYALTY),
    GENERAL(KingdomAbility.STABILITY),
    MAGISTER(KingdomAbility.CULTURE),
    TREASURER(KingdomAbility.ECONOMY),
    VICEROY(KingdomAbility.ECONOMY),
    WARDEN(KingdomAbility.STABILITY);

    companion object {
        fun fromString(value: String) = fromCamelCase<Leader>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}