package at.posselt.pfrpg2e.data

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class Currency : ValueEnum, Translatable {
    CP,
    SP,
    GP,
    PP;

    companion object {
        fun fromString(value: String) = fromCamelCase<Currency>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "currency.$value"

}