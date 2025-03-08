package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.kingdom.RawHeartland
import at.posselt.pfrpg2e.kingdom.data.RawHeartlandChoices
import at.posselt.pfrpg2e.toLabel
import js.objects.JsPlainObject

@JsPlainObject
external interface HeartlandContext {
    val type: FormElementContext
    val description: String?
    val boost: String
}

fun RawHeartlandChoices.toContext(heartlands: Array<RawHeartland>): HeartlandContext {
    val heartland = heartlands.find { it.id == type }
    return HeartlandContext(
        type = Select(
            name = "heartland.type",
            value = type,
            options = heartlands.map { SelectOption(it.name, it.id) },
            label = "Heartland",
            required = false,
        ).toContext(),
        description = heartland?.description,
        boost = heartland?.boost?.toLabel() ?: "",
    )
}