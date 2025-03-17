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

fun RawFame.toContext(maximumFamePoints: Int): FameContext {
    val fameType = FameType.fromString(type)
    val label = when (fameType) {
        FameType.FAMOUS -> "Fame"
        FameType.INFAMOUS -> "Infamy"
        else -> "Fame"
    }
    return FameContext(
        now = Select.range(
            name = "fame.now",
            label = label,
            value = now,
            from = 0,
            to = maximumFamePoints,
            stacked = false,
            elementClasses = listOf("km-width-small"),
            labelClasses = listOf("km-slim-inputs"),
        ).toContext(),
        next = Select.range(
            name = "fame.next",
            label = "Next",
            value = next,
            from = 0,
            to = maximumFamePoints,
            stacked = false,
            elementClasses = listOf("km-width-small"),
            labelClasses = listOf("km-slim-inputs"),
        ).toContext(),
        type = Select.fromEnum<FameType>(
            name = "fame.type",
            label = "Fame Type",
            value = fameType,
            stacked = false,
            hideLabel = true,
            labelFunction = { when(it) {
                FameType.FAMOUS -> "Fame"
                FameType.INFAMOUS -> "Infamy"
            }
            }
        ).toContext(),
    )
}