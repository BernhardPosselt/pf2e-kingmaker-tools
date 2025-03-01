package at.posselt.pfrpg2e.data.kingdom.leaders

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class LeaderType {
    PC,
    REGULAR_NPC,
    HIGHLY_MOTIVATED_NPC,
    NON_PATHFINDER_NPC;

    companion object {
        fun fromString(value: String) = fromCamelCase<LeaderType>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}