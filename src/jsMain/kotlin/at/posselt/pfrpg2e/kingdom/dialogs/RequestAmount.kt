package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.awaitablePrompt
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.utils.t
import js.objects.JsPlainObject

@JsPlainObject
external interface RequestAmountContext {
    val formRows: Array<FormElementContext>
}

@JsPlainObject
external interface RequestAmountData {
    val amount: Int?
}

suspend fun requestAmount(): Int {
    return awaitablePrompt<RequestAmountData, Int>(
        title = t("kingdom.howMany"),
        templateContext = RequestAmountContext(
            formRows = formContext(
                NumberInput(
                    name = "amount",
                    label = t("kingdom.amount"),
                    value = 1,
                )
            )
        ),
        templatePath = "components/forms/form.hbs",
    ) { data, _ -> data.amount ?: 1}
}