package at.posselt.pfrpg2e.data.kingdom

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class Ruin(val ability: KingdomAbility): Translatable, ValueEnum {
    CORRUPTION(KingdomAbility.CULTURE),
    CRIME(KingdomAbility.ECONOMY),
    DECAY(KingdomAbility.STABILITY),
    STRIFE(KingdomAbility.LOYALTY);

    companion object {
        fun fromString(value: String) = fromCamelCase<Ruin>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "ruin.$value"
}