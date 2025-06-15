package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.awaitablePrompt
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.utils.asAnyObject
import at.posselt.pfrpg2e.utils.t
import kotlinx.js.JsPlainObject

@Suppress("unused")
@JsPlainObject
private external interface AskRpContext {
    val formRows: Array<FormElementContext>
}

@JsPlainObject
private external interface AskRpData {
    val rp: Int
}

suspend fun askRpDialog(maxRp: Int) =
    awaitablePrompt<AskRpData, Int>(
        title = t("kingdom.rpTowardsConstruction"),
        templateContext = AskRpContext(
            formRows = arrayOf(
                Select.range(
                    name = "rp",
                    label = t("kingdom.rp"),
                    from = 1,
                    to = maxRp,
                    value = maxRp,
                ).toContext()
            )
        ).asAnyObject(),
        templatePath = "components/forms/form.hbs",
    ) { data, _ -> data.rp }
