package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.awaitablePrompt
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import com.foundryvtt.core.ui
import kotlinx.js.JsPlainObject


@JsPlainObject
external interface PickEventSettlementContext {
    val formRows: Array<FormElementContext>
}

@JsPlainObject
external interface PickEventSettlementData {
    val settlementId: String
    val secretLocation: Boolean
}

data class PickEventSettlementResult(
    val settlementId: String,
    val secretLocation: Boolean,
)

suspend fun pickEventSettlement(
    settlements: List<Settlement>,
): PickEventSettlementResult {
    if(settlements.isEmpty()) {
        val message = "Can not add Settlement event without settlements"
        ui.notifications.error(message)
        throw IllegalStateException(message)
    }
    return awaitablePrompt<PickEventSettlementData, PickEventSettlementResult>(
        title = "Choose a Settlement",
        templateContext = PickEventSettlementContext(
            formRows = formContext(
                Select(
                    name = "settlementId",
                    label = "Settlement",
                    options = settlements.map { SelectOption(value = it.id, label = it.name) }
                ),
                CheckboxInput(
                    name = "secretLocation",
                    label = "Hide Settlement from Players",
                    value = false,
                ),
            )
        ),
        templatePath = "components/forms/form.hbs",
    ) { data, _ -> PickEventSettlementResult(
        settlementId = data.settlementId,
        secretLocation = data.secretLocation,
    )}
}