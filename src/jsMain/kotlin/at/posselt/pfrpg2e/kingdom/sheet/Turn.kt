package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class Turn : Translatable, ValueEnum {
    NOW,
    NEXT;

    companion object {
        fun fromString(value: String) = fromCamelCase<Turn>(value)
    }

    override val i18nKey = "resourceButton.turn.$value"

    val i18nKeyShort = "resourceButton.turnShort.$value"

    override val value: String
        get() = toCamelCase()
}