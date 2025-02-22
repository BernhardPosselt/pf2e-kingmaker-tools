package at.posselt.pfrpg2e.data.kingdom

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.unslugify


enum class KingdomSkill {
    AGRICULTURE,
    ARTS,
    BOATING,
    DEFENSE,
    ENGINEERING,
    EXPLORATION,
    FOLKLORE,
    INDUSTRY,
    INTRIGUE,
    MAGIC,
    POLITICS,
    SCHOLARSHIP,
    STATECRAFT,
    TRADE,
    WARFARE,
    WILDERNESS;

    companion object {
        fun fromString(value: String) = fromCamelCase<KingdomSkill>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = value.unslugify()
}