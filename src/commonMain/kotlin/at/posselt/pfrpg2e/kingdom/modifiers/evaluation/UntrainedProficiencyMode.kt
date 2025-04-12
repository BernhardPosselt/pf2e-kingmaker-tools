package at.posselt.pfrpg2e.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class UntrainedProficiencyMode: ValueEnum, Translatable {
    NONE,
    HALF,
    FULL;

    companion object {
        fun fromString(value: String) = fromCamelCase<UntrainedProficiencyMode>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "untrainedProficiencyMode.$value"
}
