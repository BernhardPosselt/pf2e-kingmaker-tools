package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.kingdom.data.RawRuin
import at.posselt.pfrpg2e.kingdom.data.RawRuinValues
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RuinContext {
    val value: FormElementContext
    val penalty: FormElementContext
    val threshold: FormElementContext
}


@JsPlainObject
external interface RuinsContext {
    val corruption: RuinContext
    val crime: RuinContext
    val strife: RuinContext
    val decay: RuinContext
}

private fun RawRuinValues.toInput(key: String) =
    RuinContext(
        value= Select.range(
            from = 0,
            to = threshold,
            name = "ruin.$key.value",
            label = "Value",
            value = value
        ).toContext(),
        penalty= Select.range(
            from = 0,
            to = 4,
            name = "ruin.$key.penalty",
            label = "Penalty",
            value = penalty
        ).toContext(),
        threshold= NumberInput(
            name = "ruin.$key.threshold",
            label = "Threshold",
            value = threshold
        ).toContext(),
    )

fun RawRuin.toContext() =
    RuinsContext(
        corruption = corruption.toInput("corruption"),
        strife = strife.toInput("strife"),
        crime = crime.toInput("crime"),
        decay = decay.toInput("decay"),
    )