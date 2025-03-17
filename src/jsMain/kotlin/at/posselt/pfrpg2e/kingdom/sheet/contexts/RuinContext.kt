package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.OverrideType
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.data.kingdom.RuinValue
import at.posselt.pfrpg2e.data.kingdom.RuinValues
import at.posselt.pfrpg2e.kingdom.data.RawRuin
import at.posselt.pfrpg2e.kingdom.data.RawRuinValues
import at.posselt.pfrpg2e.toLabel
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


@JsPlainObject
external interface RuinsContext {
    val corruption: RuinContext
    val crime: RuinContext
    val strife: RuinContext
    val decay: RuinContext
}

private fun RawRuinValues.toInput(key: String, automateStats: Boolean, calculated: RuinValue) =
    RuinContext(
        value = Select.range(
            from = 0,
            to = threshold,
            name = "ruin.$key.value",
            label = key.toLabel(),
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
        label = key.toLabel(),
        thresholdValue = calculated.threshold,
    )

fun RawRuin.toContext(automateStats: Boolean, parseRuins: RuinValues) =
    RuinsContext(
        corruption = corruption.toInput("corruption", automateStats, parseRuins.corruption),
        strife = strife.toInput("strife", automateStats, parseRuins.strife),
        crime = crime.toInput("crime", automateStats, parseRuins.crime),
        decay = decay.toInput("decay", automateStats, parseRuins.decay),
    )