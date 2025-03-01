package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.prompt
import at.posselt.pfrpg2e.utils.asAnyObject
import js.objects.JsPlainObject


@JsPlainObject
private external interface AddOngoingEventContext {
    val formRows: Array<FormElementContext>
}

@JsPlainObject
private external interface AddOngoingEventData {
    val event: String
}

suspend fun addOngoingEvent(
    onOk: (String) -> Unit,
) {
    prompt<AddOngoingEventData, Unit>(
        title = "Add Ongoing Event",
        templateContext = AddOngoingEventContext(
            formRows = arrayOf(
                TextInput(
                    name = "event",
                    label = "Event",
                    value = "",
                ).toContext()
            )
        ).asAnyObject(),
        templatePath = "components/forms/form.hbs",
    ) { data ->
        onOk(data.event)
    }
}
