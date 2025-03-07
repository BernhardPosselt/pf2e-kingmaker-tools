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
            readonly = automateArmyConsumption
        ).toContext(),
        now = NumberInput(
            name = "consumption.now",
            value = now,
            label = "Now"
        ).toContext(),
        next = NumberInput(
            name = "consumption.next",
            value = next,
            label = "Next"
        ).toContext(),
    )