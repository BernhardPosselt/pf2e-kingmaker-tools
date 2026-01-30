package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.data.kingdom.Relations
import at.posselt.pfrpg2e.kingdom.data.RawGroup
import at.posselt.pfrpg2e.utils.t
import kotlinx.js.JsPlainObject

@Suppress("unused")
@JsPlainObject
external interface GroupContext {
    val name: FormElementContext
    val negotiationDC: FormElementContext
    val atWar: FormElementContext
    val preventPledgeOfFealty: FormElementContext
    val relations: FormElementContext
}

fun Array<RawGroup>.toContext() =
    mapIndexed { index, group ->
        GroupContext(
            name = TextInput(
                name = "groups.$index.name",
                label = t("applications.name"),
                hideLabel = true,
                value = group.name,
            ).toContext(),
            negotiationDC = Select.dc(
                name = "groups.$index.negotiationDC",
                label = t("kingdom.negotiationDc"),
                hideLabel = true,
                value = group.negotiationDC,
            ).toContext(),
            atWar = CheckboxInput(
                name = "groups.$index.atWar",
                label = t("kingdom.atWar"),
                hideLabel = true,
                value = group.atWar,
            ).toContext(),
            preventPledgeOfFealty = CheckboxInput(
                name = "groups.$index.preventPledgeOfFealty",
                label = t("kingdom.preventPledgeOfFealty"),
                hideLabel = true,
                value = group.preventPledgeOfFealty,
            ).toContext(),
            relations = Select.fromEnum<Relations>(
                name = "groups.$index.relations",
                hideLabel = true,
                value = Relations.fromString(group.relations) ?: Relations.NONE,
            ).toContext(),
        )
    }.toTypedArray()