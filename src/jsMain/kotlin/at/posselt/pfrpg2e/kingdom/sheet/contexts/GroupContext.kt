package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.data.kingdom.Relations
import at.posselt.pfrpg2e.kingdom.data.RawGroup
import js.objects.JsPlainObject

@JsPlainObject
external interface GroupContext  {
    val name: FormElementContext
    val negotiationDC: FormElementContext
    val atWar: FormElementContext
    val relations: FormElementContext
}

fun Array<RawGroup>.toContext() =
    mapIndexed {index, group ->
        GroupContext(
            name = TextInput(
                name = "groups.$index.name",
                label = "Name",
                hideLabel = true,
                value = group.name,
            ).toContext(),
            negotiationDC = Select.dc(
                name = "groups.$index.negotiationDC",
                label = "Negotiation DC",
                hideLabel = true,
                value = group.negotiationDC,
            ).toContext(),
            atWar = CheckboxInput(
                name = "groups.$index.atWar",
                label = "At War",
                hideLabel = true,
                value = group.atWar,
            ).toContext(),
            relations = Select.fromEnum<Relations>(
                name = "groups.$index.relations",
                label = "Relations",
                hideLabel = true,
                value = Relations.fromString(group.relations) ?: Relations.NONE,
            ).toContext(),
        )
    }.toTypedArray()