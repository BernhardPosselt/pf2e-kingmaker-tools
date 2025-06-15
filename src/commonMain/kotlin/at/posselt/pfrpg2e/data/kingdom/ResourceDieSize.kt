package at.posselt.pfrpg2e.data.kingdom

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class ResourceDieSize(
    val faceSize: Int,
): Translatable, ValueEnum {
    D4(4),
    D6(6),
    D8(8),
    D10(10),
    D12(12);

    companion object {
        fun fromString(value: String) = fromCamelCase<ResourceDieSize>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "diceFaces.$value"

    fun formula(amount: Int) = "$amount$value"
}