package at.posselt.pfrpg2e.data.kingdom.leaders

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class LeaderType: Translatable, ValueEnum {
    PC,
    REGULAR_NPC,
    HIGHLY_MOTIVATED_NPC,
    NON_PATHFINDER_NPC;

    companion object {
        fun fromString(value: String) = fromCamelCase<LeaderType>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "leaderType.$value"
}