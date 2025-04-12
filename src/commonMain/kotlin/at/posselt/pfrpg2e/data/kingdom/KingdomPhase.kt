package at.posselt.pfrpg2e.data.kingdom

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class KingdomPhase: Translatable, ValueEnum {
    ARMY,
    CIVIC,
    COMMERCE,
    EVENT,
    LEADERSHIP,
    REGION,
    UPKEEP;

    companion object {
        fun fromString(value: String) = fromCamelCase<KingdomPhase>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "kingdomPhase.$value"
}
