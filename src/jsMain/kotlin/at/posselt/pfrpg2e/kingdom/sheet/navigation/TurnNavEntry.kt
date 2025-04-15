package at.posselt.pfrpg2e.kingdom.sheet.navigation

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

@Suppress("unused")
enum class TurnNavEntry : ValueEnum, Translatable {
    UPKEEP,
    COMMERCE,
    LEADERSHIP,
    REGION,
    CIVIC,
    ARMY,
    EVENT,
    XP,
    END;

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "kingdomTurnNav.$value"
}