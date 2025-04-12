package at.posselt.pfrpg2e.data.kingdom

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class Relations: Translatable, ValueEnum {
    NONE,
    DIPLOMATIC_RELATIONS,
    TRADE_AGREEMENT;

    companion object {
        fun fromString(value: String) = fromCamelCase<Relations>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "kingdomRelations.$value"
}