package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.kingdom.data.RawConsumption
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ConsumptionContext {
    var armies: FormElementContext
    var now: FormElementContext
    var next: FormElementContext
}

fun RawConsumption.toContext(automateArmyConsumption: Boolean) =
    ConsumptionContext(
        armies = NumberInput(
            name = "consumption.armies",
            value = armies,
            label = "Armies",
            readonly = automateArmyConsumption,
            elementClasses = listOf("km-slim-inputs", "km-width-small"),
        ).toContext(),
        now = NumberInput(
            name = "consumption.now",
            value = now,
            label = "Consumption",
            elementClasses = listOf("km-slim-inputs", "km-width-small"),
        ).toContext(),
        next = NumberInput(
            name = "consumption.next",
            value = next,
            label = "Next",
            elementClasses = listOf("km-slim-inputs", "km-width-small"),
        ).toContext(),
    )