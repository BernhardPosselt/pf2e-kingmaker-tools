package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.deCamelCase
import at.posselt.pfrpg2e.kingdom.data.RawResources
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ResourceContext {
    var now: FormElementContext
    var next: FormElementContext
}

fun RawResources.toContext(key: String) =
    ResourceContext(
        now = NumberInput(
            name = "$key.now",
            label = key.deCamelCase(),
            value = now,
            stacked = false,
            elementClasses = listOf("km-width-small"),
            labelClasses = listOf("km-slim-inputs"),
        ).toContext(),
        next = NumberInput(
            name = "$key.next",
            label = "Next",
            value = next,
            stacked = false,
            elementClasses = listOf("km-width-small"),
            labelClasses = listOf("km-slim-inputs"),
        ).toContext(),
    )