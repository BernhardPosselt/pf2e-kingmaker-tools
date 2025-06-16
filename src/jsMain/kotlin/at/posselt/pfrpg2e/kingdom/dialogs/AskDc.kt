package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.awaitablePrompt
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.utils.t
import js.objects.recordOf
import kotlinx.js.JsPlainObject

@JsPlainObject
private external interface AskDcData {
    val dc: Int
}


suspend fun askDc(activityName: String) = awaitablePrompt<AskDcData, Int>(
    title = t("kingdom.selectKingdomActivityDc", recordOf("activityName" to activityName)),
    templatePath = "components/forms/form.hbs",
    templateContext = recordOf(
        "formRows" to formContext(Select.dc())
    ),
) { it, _ ->
    it.dc
}