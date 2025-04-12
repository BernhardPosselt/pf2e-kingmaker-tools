package at.posselt.pfrpg2e.data.events

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class KingdomEventTrait: Translatable, ValueEnum {
    DOWNTIME,
    LEADERSHIP,
    HEX,
    DANGEROUS,
    BENEFICIAL,
    SETTLEMENT,
    CONTINUOUS;

    companion object {
        fun fromString(value: String) = fromCamelCase<KingdomEventTrait>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "kingdomEventTrait.$value"
}