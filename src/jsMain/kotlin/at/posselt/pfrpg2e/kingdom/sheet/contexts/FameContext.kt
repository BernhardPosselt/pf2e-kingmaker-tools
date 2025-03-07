package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.data.kingdom.FameType
import at.posselt.pfrpg2e.kingdom.data.RawFame
import js.objects.JsPlainObject

@JsPlainObject
external interface FameContext {
    val now: FormElementContext
    val next: FormElementContext
    val type: FormElementContext
}

fun RawFame.toContext(maximumFamePoints: Int) =
    FameContext(
        now = Select.range(
            name = "fame.now",
            label = "Fame",
            value = now,
            from = 0,
            to = maximumFamePoints,
        ).toContext(),
        next = Select.range(
            name = "fame.next",
            label = "Fame Next",
            value = next,
            from = 0,
            to = maximumFamePoints,
        ).toContext(),
        type = Select.fromEnum<FameType>(
            name = "fame.type",
            label = "Fame Type",
            value = FameType.fromString(type),
        ).toContext(),
    )