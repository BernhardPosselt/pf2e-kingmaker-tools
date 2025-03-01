package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.OverrideType
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.kingdom.Settlement
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.launch
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.core.utils.deepClone
import js.core.Void
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.Int
import kotlin.js.Promise

@JsPlainObject
private external interface EditSettlementContext : HandlebarsRenderContext {
    val isFormValid: Boolean
    val formRows: Array<FormElementContext>
}

@JsPlainObject
private external interface EditSettlementData {
    var lots: Int
    var level: Int
    var type: String
    var secondaryTerritory: Boolean
    var manualSettlementLevel: Boolean
    var waterBorders: Int
}

@JsExport
class EditSettlementModel(val value: AnyObject) : DataModel(value) {
    companion object {
        @Suppress("unused")
        @JsStatic
        fun defineSchema() = buildSchema {
            int("lots") {
                min = 0
            }
            int("level")
            enum<SettlementType>("type")
            boolean("secondaryTerritory")
            boolean("manualSettlementLevel")
            int("waterBorders")
        }
    }
}

private class ConfigureEditSettlement(
    private val autoLevel: Boolean,
    settlementName: String,
    settlement: Settlement,
    private val onSave: (Settlement) -> Unit,
) : FormApp<EditSettlementContext, EditSettlementData>(
    title = "Edit Settlement: $settlementName",
    template = "components/forms/application-form.hbs",
    debug = true,
    dataModel = EditSettlementModel::class.js,
    id = "kmEditSettlement",
) {
    val settlementCopy = deepClone(settlement)
    var data = EditSettlementData(
        lots = settlement.lots,
        level = settlement.level,
        type = settlement.type,
        secondaryTerritory = settlement.secondaryTerritory,
        manualSettlementLevel = settlement.manualSettlementLevel == true,
        waterBorders = settlement.waterBorders,
    )

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "save" -> {
                settlementCopy.lots = data.lots
                settlementCopy.level = data.level
                settlementCopy.type = data.type
                settlementCopy.secondaryTerritory = data.secondaryTerritory
                settlementCopy.manualSettlementLevel = data.manualSettlementLevel
                settlementCopy.waterBorders = data.waterBorders
                onSave(settlementCopy)
                close()
            }
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<EditSettlementContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val rows = formContext(
            Select.fromEnum<SettlementType>(
                name = "type",
                value = fromCamelCase<SettlementType>(data.type),
                label = "Type",
            ),
            Select.range(
                name = "waterBorders",
                label = "Water Borders",
                from = 0,
                to = 4,
                value = data.waterBorders,
            ),
            CheckboxInput(
                name = "secondaryTerritory",
                label = "In Secondary Territory",
                value = data.secondaryTerritory,
            ),
            CheckboxInput(
                name = "manualSettlementLevel",
                label = "Manual Settlement Management",
                value = data.manualSettlementLevel == true,
                help = "If enabled, allows you to edit lots and level manually",
            ),

            )
        val manualElements = if (data.manualSettlementLevel || !autoLevel) {
            formContext(
                Select.range(
                    name = "level",
                    label = "Level",
                    from = 1,
                    to = 20,
                    value = data.level,
                ),
                NumberInput(
                    name = "lots",
                    label = "Lots",
                    value = data.lots,
                ),
            )
        } else {
            formContext(
                HiddenInput(
                    name = "level",
                    label = "Level",
                    value = data.level.toString(),
                    overrideType = OverrideType.NUMBER,
                ),
                HiddenInput(
                    name = "lots",
                    label = "Lots",
                    value = data.lots.toString(),
                    overrideType = OverrideType.NUMBER,
                ),
            )
        }
        EditSettlementContext(
            partId = parent.partId,
            formRows = rows + manualElements,
            isFormValid = true,
        )
    }

    override fun onParsedSubmit(value: EditSettlementData): Promise<Void> = buildPromise {
        data = value
        null
    }
}

fun editSettlement(
    autoLevel: Boolean,
    settlementName: String,
    settlement: Settlement,
    onOk: (Settlement) -> Unit,
) {
    ConfigureEditSettlement(
        autoLevel,
        settlementName,
        settlement,
        onOk,
    ).launch()
}