package at.posselt.pfrpg2e.data.kingdom.leaders

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.data.kingdom.KingdomAbility
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class Leader(val keyAbility: KingdomAbility): Translatable, ValueEnum {
    RULER(KingdomAbility.LOYALTY),
    COUNSELOR(KingdomAbility.CULTURE),
    EMISSARY(KingdomAbility.LOYALTY),
    GENERAL(KingdomAbility.STABILITY),
    MAGISTER(KingdomAbility.CULTURE),
    TREASURER(KingdomAbility.ECONOMY),
    VICEROY(KingdomAbility.ECONOMY),
    WARDEN(KingdomAbility.STABILITY);

    companion object {
        fun fromString(value: String) = fromCamelCase<Leader>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "leader.$value"
}