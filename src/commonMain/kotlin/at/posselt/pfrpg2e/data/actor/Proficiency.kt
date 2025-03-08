package at.posselt.pfrpg2e.data.actor

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class Proficiency(val increaseLockedUntil: Int = 0) {
    UNTRAINED,
    TRAINED,
    EXPERT,
    MASTER(7),
    LEGENDARY(15);

    companion object {
        fun fromString(value: String) = fromCamelCase<Proficiency>(value)
        fun fromRank(value: Int) = entries[value]
    }

    val rank: Int
        get() = ordinal

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}

fun findHighestProficiency(level: Int) : Proficiency? =
    Proficiency.entries
        .filter { it.increaseLockedUntil <= level }
        .maxByOrNull { it.rank }