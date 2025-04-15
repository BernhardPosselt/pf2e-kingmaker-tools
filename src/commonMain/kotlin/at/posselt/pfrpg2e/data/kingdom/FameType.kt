package at.posselt.pfrpg2e.data.kingdom

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

@Suppress("unused")
enum class FameType: Translatable, ValueEnum {
    FAMOUS,
    INFAMOUS;

    companion object {
        fun fromString(value: String) = fromCamelCase<FameType>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "fameType.$value"
}