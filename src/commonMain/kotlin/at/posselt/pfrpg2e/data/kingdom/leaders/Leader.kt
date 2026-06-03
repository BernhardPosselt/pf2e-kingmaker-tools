package at.posselt.pfrpg2e.data.kingdom.leaders

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.data.kingdom.KingdomAbility
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class Leader(val keyAbility: KingdomAbility, val vacancyPenalty: Int): Translatable, ValueEnum {
    RULER(KingdomAbility.LOYALTY, -1),
    COUNSELOR(KingdomAbility.CULTURE, -1),
    EMISSARY(KingdomAbility.LOYALTY, -1),
    GENERAL(KingdomAbility.STABILITY, -4),
    MAGISTER(KingdomAbility.CULTURE, -4),
    TREASURER(KingdomAbility.ECONOMY, -1),
    VICEROY(KingdomAbility.ECONOMY, -1),
    WARDEN(KingdomAbility.STABILITY, -4);

    companion object {
        fun fromString(value: String) = fromCamelCase<Leader>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "leader.$value"
}