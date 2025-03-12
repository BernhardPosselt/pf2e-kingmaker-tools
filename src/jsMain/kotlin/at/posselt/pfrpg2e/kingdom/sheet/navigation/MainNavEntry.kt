package at.posselt.pfrpg2e.kingdom.sheet.navigation

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel

enum class MainNavEntry {
    TURN, KINGDOM, SETTLEMENTS, TRADE_AGREEMENTS, MODIFIERS, NOTES;

    companion object {
        fun fromString(value: String) = fromCamelCase<MainNavEntry>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}