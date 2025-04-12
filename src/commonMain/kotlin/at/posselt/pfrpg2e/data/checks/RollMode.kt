package at.posselt.pfrpg2e.data.checks

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class RollMode(val label: String): Translatable, ValueEnum {
    PUBLICROLL("Public Roll"),
    GMROLL("GM Roll"),
    BLINDROLL("Blind Roll"),
    SELFROLL("Self Roll");

    companion object {
        fun fromString(value: String) = fromCamelCase<RollMode>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "rollMode.$value"
}