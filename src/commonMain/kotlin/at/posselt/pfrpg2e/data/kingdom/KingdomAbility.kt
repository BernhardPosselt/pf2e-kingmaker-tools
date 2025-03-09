package at.posselt.pfrpg2e.data.kingdom

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class KingdomAbility(val ruin: Ruin) {
    CULTURE(Ruin.CORRUPTION),
    ECONOMY(Ruin.CRIME),
    LOYALTY(Ruin.STRIFE),
    STABILITY(Ruin.DECAY);

    companion object {
        fun fromString(value: String) = fromCamelCase<KingdomAbility>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}

fun calculateScore(boosts: Int, flaws: Int): Int {
    val total = boosts - flaws
    return if (total < 4) {
        10 + total * 2
    } else {
        18 + (total - 4)
    }
}