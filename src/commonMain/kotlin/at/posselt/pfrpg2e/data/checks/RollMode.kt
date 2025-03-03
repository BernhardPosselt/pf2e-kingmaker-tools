package at.posselt.pfrpg2e.data.checks

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase

enum class RollMode(val label: String) {
    PUBLICROLL("Public Roll"),
    GMROLL("GM Roll"),
    BLINDROLL("Blind Roll"),
    SELFROLL("Self Roll");

    companion object {
        fun fromString(value: String) = fromCamelCase<RollMode>(value)
    }

    val value: String
        get() = toCamelCase()
}