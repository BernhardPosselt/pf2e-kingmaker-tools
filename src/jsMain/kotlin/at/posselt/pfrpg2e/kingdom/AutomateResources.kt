package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class AutomateResources: ValueEnum, Translatable {
    KINGMAKER,
    TILE_BASED,
    MANUAL;

    companion object {
        fun fromString(value: String) = fromCamelCase<AutomateResources>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "automateResources.$value"
}