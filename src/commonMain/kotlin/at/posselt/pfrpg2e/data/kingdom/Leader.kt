package at.posselt.pfrpg2e.data.kingdom

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.unslugify


enum class Leader {
    RULER,
    COUNSELOR,
    EMISSARY,
    GENERAL,
    MAGISTER,
    TREASURER,
    VICEROY,
    WARDEN;

    companion object {
        fun fromString(value: String) = fromCamelCase<KingdomSkill>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = value.unslugify()
}