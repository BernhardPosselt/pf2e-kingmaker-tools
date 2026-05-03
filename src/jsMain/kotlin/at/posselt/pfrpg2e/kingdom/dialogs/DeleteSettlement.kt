package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.awaitablePrompt
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.utils.asAnyObject
import at.posselt.pfrpg2e.utils.t
import js.objects.recordOf
import kotlinx.js.JsPlainObject

@Suppress("unused")
@JsPlainObject
external interface DeleteSettlementContext {
    val formRows: Array<FormElementContext>
}

@JsPlainObject
external interface DeleteSettlementData {
    val deleteScene: Boolean
}

suspend fun deleteSettlementDialog(
    settlementName: String,
    onOk: suspend (deleteScene: Boolean) -> Unit
) = awaitablePrompt<DeleteSettlementData, Unit>(
    title = t("kingdom.deleteSettlement", recordOf("name" to settlementName)),
    templateContext = DeleteSettlementContext(
        formRows = arrayOf(
            CheckboxInput(
                name = "deleteScene",
                label = t("kingdom.deleteSettlementScene"),
                value = false,
            ).toContext()
        )
    ).asAnyObject(),
    width = 400,
    templatePath = "components/forms/form.hbs",
    buttonLabel = t("applications.delete")
) { data, _ -> onOk(data.deleteScene) }
