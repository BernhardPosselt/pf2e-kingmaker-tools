package at.posselt.pfrpg2e.data.kingdom

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class KingdomSizeType: Translatable, ValueEnum {
    TERRITORY,
    PROVINCE,
    STATE,
    COUNTRY,
    DOMINION;

    companion object {
        fun fromString(value: String) = fromCamelCase<KingdomSizeType>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "kingdomSizeType.$value"
}