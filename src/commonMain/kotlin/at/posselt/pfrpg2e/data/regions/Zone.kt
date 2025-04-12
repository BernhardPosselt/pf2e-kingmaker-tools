package at.posselt.pfrpg2e.data.regions

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase


enum class Terrain: ValueEnum, Translatable {
    AQUATIC,
    DESERT,
    PLAINS,
    HILLS,
    FOREST,
    MOUNTAIN,
    SWAMP,
    URBAN,
    DUNGEON;

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "zoneTerrain.$value"
}
