package at.posselt.pfrpg2e.data.actor

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class Proficiency(val increaseLockedUntil: Int = 0): Translatable, ValueEnum {
    UNTRAINED,
    TRAINED,
    EXPERT,
    MASTER(7),
    LEGENDARY(15);

    companion object {
        fun fromString(value: String) = fromCamelCase<Proficiency>(value)
        fun fromRank(value: Int) = entries[value]
    }

    val rank: Int
        get() = ordinal

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "proficiency.$value"
}

fun findHighestProficiency(level: Int) : Proficiency? =
    Proficiency.entries
        .filter { it.increaseLockedUntil <= level }
        .maxByOrNull { it.rank }

val highestProficiencyByLevel = (1..20)
    .mapNotNull { it to findHighestProficiency(it) }
    .toMap()