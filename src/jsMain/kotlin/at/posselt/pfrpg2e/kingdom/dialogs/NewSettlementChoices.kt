package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.awaitablePrompt
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.data.kingdom.settlements.SettlementLayoutType
import at.posselt.pfrpg2e.kingdom.SettlementTerrain
import at.posselt.pfrpg2e.utils.asAnyObject
import at.posselt.pfrpg2e.utils.t
import kotlinx.js.JsPlainObject


@Suppress("unused")
@JsPlainObject
external interface NewSettlementChoicesContext {
    val formRows: Array<FormElementContext>
}

@JsPlainObject
external interface NewSettlementChoicesData {
    val name: String
    val waterBorders: Int
    val terrain: String
    val layoutType: String
}

data class NewSettlementChoices(
    val name: String,
    val waterBorders: Int,
    val terrain: SettlementTerrain,
    val layoutType: SettlementLayoutType,
)

suspend fun newSettlementChoices(terrain: SettlementTerrain? = null): NewSettlementChoices =
    awaitablePrompt<NewSettlementChoicesData, NewSettlementChoices>(
        title = t("kingdom.newSettlement"),
        templateContext = NewSettlementChoicesContext(
            formRows = formContext(
                TextInput(
                    name = "name",
                    label = t("applications.name"),
                    value = "",
                ),
                Select.range(
                    name = "waterBorders",
                    label = t("kingdom.waterBorders"),
                    from = 0,
                    to = 4,
                    value = 0,
                ),
                Select.fromEnum<SettlementTerrain>(
                    name = "terrain",
                    value = terrain ?: SettlementTerrain.FOREST,
                ),
                Select.fromEnum<SettlementLayoutType>(
                    name = "layoutType",
                    value = SettlementLayoutType.RIGID,
                    help = t("kingdom.settlementLayoutTypeHelp")
                )
            )
        ).asAnyObject(),
        templatePath = "components/forms/form.hbs",
        width = 400,
    ) { data, _ -> NewSettlementChoices(
        name = data.name,
        waterBorders = data.waterBorders,
        terrain = SettlementTerrain.fromString(data.terrain) ?: SettlementTerrain.FOREST,
        layoutType = SettlementLayoutType.fromString(data.layoutType) ?: SettlementLayoutType.RIGID,
    ) }
