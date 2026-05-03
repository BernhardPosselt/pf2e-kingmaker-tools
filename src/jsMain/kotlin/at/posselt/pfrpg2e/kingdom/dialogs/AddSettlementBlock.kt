package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.awaitablePrompt
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.kingdom.structures.SettlementBlockShape
import at.posselt.pfrpg2e.utils.asAnyObject
import at.posselt.pfrpg2e.utils.t
import kotlinx.js.JsPlainObject

@Suppress("unused")
@JsPlainObject
external interface AddSettlementBlockContext {
    val formRows: Array<FormElementContext>
}

@JsPlainObject
external interface AddSettlementBlockData {
    val shape: String
    val x: Int
    val y: Int
}

data class AddSettlementBlockChoices(
    val x: Int,
    val y: Int,
    val shape: SettlementBlockShape,
)

suspend fun addSettlementBlockDialog(
    onOk: suspend (data: AddSettlementBlockChoices) -> Unit
) = awaitablePrompt<AddSettlementBlockData, Unit>(
    title = t("addSettlementBlock.title"),
    templateContext = AddSettlementBlockContext(
        formRows = arrayOf(
            Select.fromEnum<SettlementBlockShape>(
                name = "shape",
                value = SettlementBlockShape.TWO_BY_TWO,
                stacked = false,
            ).toContext(),
            NumberInput(
                name = "x",
                label = t("addSettlementBlock.x"),
                value = 8,
                stacked = false,
            ).toContext(),
            NumberInput(
                name = "y",
                label = t("addSettlementBlock.y"),
                value = 8,
                stacked = false,
            ).toContext()
        )
    ).asAnyObject(),
    width = 400,
    templatePath = "components/forms/form.hbs",
    buttonLabel = t("applications.add")
) { data, _ ->
    onOk(
        AddSettlementBlockChoices(
            x = data.x,
            y = data.y,
            shape = SettlementBlockShape.fromString(data.shape)!!,
        )
    )
}
