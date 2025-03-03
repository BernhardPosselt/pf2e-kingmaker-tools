package at.posselt.pfrpg2e.data.kingdom.activities

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class ActivityDcType {
    VALUE,
    CONTROL,
    CUSTOM,
    NONE,
    SCOUTING;

    companion object {
        fun fromString(value: String) = fromCamelCase<ActivityDcType>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}