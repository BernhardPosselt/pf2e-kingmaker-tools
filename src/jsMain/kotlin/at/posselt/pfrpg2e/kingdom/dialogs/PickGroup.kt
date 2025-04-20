package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.awaitablePrompt
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.kingdom.data.RawGroup
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.ui
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface PickGroupContext {
    val formRows: Array<FormElementContext>
}

@JsPlainObject
external interface PickGroupData {
    val index: Int
}

suspend fun pickGroup(groups: Array<RawGroup>): RawGroup {
    if (groups.isEmpty()) {
        val message = t("kingdom.noGroupsAvailable")
        ui.notifications.error(message)
        throw IllegalArgumentException(message)
    }
    val sortedGroups = groups.sortedBy { it.name }
    return awaitablePrompt<PickGroupData, RawGroup>(
        title = t("kingdom.pickGroup"),
        templateContext = PickGroupContext(
            formRows = formContext(
                Select(
                    name = "index",
                    value = "0",
                    label = t("kingdom.group"),
                    options = sortedGroups.mapIndexed { index, group ->
                        SelectOption(
                            value = index.toString(),
                            label = group.name
                        )
                    }
                )
            )
        ),
        templatePath = "components/forms/form.hbs",
    ) { data, _ -> sortedGroups[data.index] }
}