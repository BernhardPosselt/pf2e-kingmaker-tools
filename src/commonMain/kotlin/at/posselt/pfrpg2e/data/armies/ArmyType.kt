package at.posselt.pfrpg2e.data.armies

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class ArmyType: Translatable, ValueEnum {
    SKIRMISHER,
    CAVALRY,
    SIEGE,
    INFANTRY;

    companion object {
        fun fromString(value: String) = fromCamelCase<ArmyType>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "armyType.$value"
}