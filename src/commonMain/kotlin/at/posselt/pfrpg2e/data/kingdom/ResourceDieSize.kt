package at.posselt.pfrpg2e.data.kingdom

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class ResourceDieSize {
    D4,
    D6,
    D8,
    D10,
    D12;

    companion object {
        fun fromString(value: String) = fromCamelCase<ResourceDieSize>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}