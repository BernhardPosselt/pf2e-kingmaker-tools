package at.posselt.pfrpg2e.data.kingdom

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class ResourceDieSize: Translatable, ValueEnum {
    D4,
    D6,
    D8,
    D10,
    D12;

    companion object {
        fun fromString(value: String) = fromCamelCase<ResourceDieSize>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "diceFaces.$value"

    fun formula(amount: Int) = "$amount$value"
}