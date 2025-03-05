package at.posselt.pfrpg2e.data.kingdom

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class WorksiteType {
    MINE,
    LUMBERCAMP,
    QUARRY,
    LUXURYWORKSITE,
    FARMLAND;

    companion object {
        fun fromString(value: String) = fromCamelCase<WorksiteType>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}