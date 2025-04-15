package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.OverrideType
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.data.kingdom.Ruin
import at.posselt.pfrpg2e.data.kingdom.RuinValue
import at.posselt.pfrpg2e.data.kingdom.RuinValues
import at.posselt.pfrpg2e.kingdom.data.RawRuin
import at.posselt.pfrpg2e.kingdom.data.RawRuinValues
import at.posselt.pfrpg2e.utils.t
import kotlinx.js.JsPlainObject

@Suppress("unused")
@JsPlainObject
external interface RuinContext {
    val value: FormElementContext
    val penalty: FormElementContext
    val threshold: FormElementContext
    val thresholdValue: Int
    val label: String
}


private fun RawRuinValues.toInput(
    key: String,
    automateStats: Boolean,
    calculated: RuinValue,
    label: String,
) =
    RuinContext(
        value = Select.range(
            from = 0,
            to = threshold,
            name = "ruin.$key.value",
            label = label,
            value = value,
            stacked = false,
            elementClasses = listOf("km-width-small"),
            labelClasses = listOf("km-slim-inputs"),
        ).toContext(),
        penalty = Select.range(
            from = 0,
            to = 4,
            name = "ruin.$key.penalty",
            label = "Penalty",
            value = penalty,
            stacked = false,
            hideLabel = true,
            elementClasses = listOf("km-slim-inputs", "km-width-small"),
        ).toContext(),
        threshold = if (automateStats) {
            HiddenInput(
                name = "ruin.$key.threshold",
                value = threshold.toString(),
                hideLabel = true,
                overrideType = OverrideType.NUMBER,
            ).toContext()
        } else {
            NumberInput(
                name = "ruin.$key.threshold",
                label = "Threshold",
                value = threshold,
                stacked = false,
                hideLabel = true,
                elementClasses = listOf("km-slim-inputs", "km-width-small"),
            ).toContext()
        },
        label = label,
        thresholdValue = calculated.threshold,
    )

fun RawRuin.toContext(automateStats: Boolean, parseRuins: RuinValues) =
    arrayOf(
        corruption.toInput("corruption", automateStats, parseRuins.corruption, t(Ruin.CORRUPTION)),
        crime.toInput("crime", automateStats, parseRuins.crime, t(Ruin.CRIME)),
        decay.toInput("decay", automateStats, parseRuins.decay, t(Ruin.DECAY)),
        strife.toInput("strife", automateStats, parseRuins.strife, t(Ruin.STRIFE)),
    )