package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.awaitablePrompt
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.kingdom.SettlementTerrain
import at.posselt.pfrpg2e.utils.asAnyObject
import js.objects.JsPlainObject


@Suppress("unused")
@JsPlainObject
private external interface NewSettlementChoicesContext {
    val formRows: Array<FormElementContext>
}

@JsPlainObject
private external interface NewSettlementChoicesData {
    val name: String
    val waterBorders: Int
    val terrain: String
}

data class NewSettlementChoices(
    val name: String,
    val waterBorders: Int,
    val terrain: SettlementTerrain,
)

suspend fun newSettlementChoices(terrain: SettlementTerrain? = null): NewSettlementChoices =
    awaitablePrompt<NewSettlementChoicesData, NewSettlementChoices>(
        title = "New Settlement",
        templateContext = NewSettlementChoicesContext(
            formRows = formContext(
                TextInput(
                    name = "name",
                    label = "Name",
                    value = "",
                ),
                Select.range(
                    name = "waterBorders",
                    label = "Water Borders",
                    from = 0,
                    to = 4,
                    value = 0,
                ),
                Select.fromEnum<SettlementTerrain>(
                    name = "terrain",
                    label = "Map",
                    value = terrain ?: SettlementTerrain.FOREST,
                )
            )
        ).asAnyObject(),
        templatePath = "components/forms/form.hbs",
    ) { data, _ -> NewSettlementChoices(
        name = data.name,
        waterBorders = data.waterBorders,
        terrain = SettlementTerrain.fromString(data.terrain) ?: SettlementTerrain.FOREST,
    ) }
