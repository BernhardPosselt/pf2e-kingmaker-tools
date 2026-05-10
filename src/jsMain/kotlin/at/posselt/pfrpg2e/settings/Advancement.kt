package at.posselt.pfrpg2e.settings

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class Advancement: Translatable, ValueEnum {
    XP,
    MILESTONE;

    companion object {
        fun fromString(value: String) = fromCamelCase<Advancement>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "advancement.$value"
}