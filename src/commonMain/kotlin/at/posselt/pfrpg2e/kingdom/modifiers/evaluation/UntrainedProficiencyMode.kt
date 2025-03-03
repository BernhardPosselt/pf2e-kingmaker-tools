package at.posselt.pfrpg2e.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class UntrainedProficiencyMode {
    NONE,
    HALF,
    FULL;

    companion object {
        fun fromString(value: String) = fromCamelCase<UntrainedProficiencyMode>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}
