package at.posselt.pfrpg2e.data.kingdom.settlements

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class SettlementType: Translatable, ValueEnum {
    SETTLEMENT,
    CAPITAL;

    companion object {
        fun fromString(value: String) = fromCamelCase<SettlementType>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "settlementType.$value"
}