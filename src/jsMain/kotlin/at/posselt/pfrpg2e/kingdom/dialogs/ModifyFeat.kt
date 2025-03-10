package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.TextArea
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.kingdom.RawFeat
import at.posselt.pfrpg2e.kingdom.RawRuinThresholdIncreases
import at.posselt.pfrpg2e.slugify
import at.posselt.pfrpg2e.utils.buildPromise
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
import kotlin.js.Promise

@JsPlainObject
external interface ModifyFeatContext : HandlebarsRenderContext {
    val isFormValid: Boolean
    val formRows: Array<FormElementContext>
}

@JsPlainObject
external interface ModifyFeatData {
    val name: String
    val id: String
    val text: String
    val level: Int
    val prerequisites: String?
    val resourceDice: Int
    val settlementItemLevelIncrease: Int
    val flag: String?
    val increaseAnarchyLimit: Int
    val ruinThresholdIncreasesAmount: Int
    val ruinThresholdIncreasesValue: Int
}

@JsExport
class ModifyFeatDataModel(value: AnyObject) : DataModel(value) {
    companion object {
        @Suppress("unused")
        @JsStatic
        fun defineSchema() = buildSchema {
            string("id")
            string("name")
            string("text")
            int("level")
            string("prerequisites", nullable = true)
            int("resourceDice")
            int("settlementItemLevelIncrease")
            string("flag", nullable = true)
            int("increaseAnarchyLimit")
            int("ruinThresholdIncreasesAmount")
            int("ruinThresholdIncreasesValue")
        }
    }
}


class ModifyFeat(
    data: RawFeat? = null,
    private val afterSubmit: suspend (data: RawFeat) -> Unit,
) : FormApp<ModifyFeatContext, ModifyFeatData>(
    title = if (data == null) "Add Feat" else "Edit Feat",
    template = "components/forms/application-form.hbs",
    debug = true,
    dataModel = ModifyFeatDataModel::class.js,
    id = "kmModifyFeat"
) {
    private val edit: Boolean = data != null
    private var current: RawFeat = data?.let(::deepClone) ?: RawFeat(
        name = data?.name ?: "",
        id = data?.id ?: "",
        text = data?.text ?: "",
        level = data?.level ?: 1,
        prerequisites = data?.prerequisites,
        automationNotes = data?.automationNotes,
        modifiers = data?.modifiers,
        resourceDice = data?.resourceDice,
        settlementItemLevelIncrease = data?.settlementItemLevelIncrease,
        trainSkill = data?.trainSkill,
        assuranceForSkill = data?.assuranceForSkill,
        increaseUsableSkills = data?.increaseUsableSkills,
        flags = data?.flags,
        increaseAnarchyLimit = data?.increaseAnarchyLimit,
        ruinThresholdIncreases = data?.ruinThresholdIncreases,
    )

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "save" -> save()
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<ModifyFeatContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        ModifyFeatContext(
            partId = parent.partId,
            isFormValid = isFormValid,
            formRows = formContext(
                TextInput(
                    name = "id",
                    value = current.id,
                    label = "Id",
                    help = "Choose the same Id as an existing charter to override it",
                    readonly = edit == true,
                    stacked = false
                ),
                TextInput(
                    name = "name",
                    value = current.name,
                    label = "Name",
                    stacked = false
                ),
                TextArea(
                    name = "text",
                    value = current.text,
                    label = "Description",
                    stacked = false
                ),
                Select.range(
                    from = 1,
                    to = 20,
                    name = "level",
                    value = current.level,
                    label = "Level",
                    stacked = false
                ),
                TextInput(
                    name = "prerequisites",
                    value = current.prerequisites ?: "",
                    label = "Prerequisites",
                    stacked = false,
                    required = false,
                ),
                NumberInput(
                    name = "resourceDice",
                    value = current.resourceDice ?: 0,
                    label = "Bonus Resource Dice",
                    stacked = false
                ),
                NumberInput(
                    name = "settlementItemLevelIncrease",
                    value = current.settlementItemLevelIncrease ?: 0,
                    label = "Increase Level of Magic Items in Settlements",
                    stacked = false
                ),
                NumberInput(
                    name = "increaseAnarchyLimit",
                    value = current.increaseAnarchyLimit ?: 0,
                    label = "Increase Anarchy Limit",
                    stacked = false
                ),
                NumberInput(
                    name = "ruinThresholdIncreasesAmount",
                    value = current.ruinThresholdIncreases?.firstOrNull()?.amount ?:0,
                    label = "Ruin Threshold Increases",
                    help = "Number of eligible Ruin Thresholds increases; only used if a value is provided as well",
                    stacked = false,
                ),
                NumberInput(
                    name = "ruinThresholdIncreasesValue",
                    value = current.ruinThresholdIncreases?.firstOrNull()?.increase ?:0,
                    label = "Ruin Threshold Increase Value",
                    help = "Adds this value to each increased Ruin Threshold",
                    stacked = false
                ),
                TextInput(
                    name = "flag",
                    value = current.flags?.firstOrNull() ?: "",
                    label = "Flag",
                    stacked = false,
                    required = false,
                    help = "Can be matched against in modifiers when selecting eligible modifiers."
                ),
            )
        )
    }

    fun save(): Promise<Void> = buildPromise {
        if (isValid()) {
            close().await()
            current.ruinThresholdIncreases?.filter { it.increase > 0 && it.amount > 0 }?.toTypedArray() ?: emptyArray()
            afterSubmit(current)
        }
        undefined
    }

    override fun onParsedSubmit(value: ModifyFeatData): Promise<Void> = buildPromise {
        current = RawFeat(
            name = value.name,
            id = value.id.slugify(),
            text = value.text,
            level = value.level,
            prerequisites = value.prerequisites,
            automationNotes = current.automationNotes,
            modifiers = current.modifiers,
            resourceDice = value.resourceDice,
            settlementItemLevelIncrease = value.settlementItemLevelIncrease,
            trainSkill = current.trainSkill,
            assuranceForSkill = current.assuranceForSkill,
            increaseUsableSkills = current.increaseUsableSkills,
            flags = value.flag?.takeIf { it.isNotEmpty() }?.let { arrayOf(it) } ?: emptyArray(),
            increaseAnarchyLimit = value.increaseAnarchyLimit,
            ruinThresholdIncreases = if (value.ruinThresholdIncreasesAmount > 0 || value.ruinThresholdIncreasesValue > 0) {
                arrayOf(
                    RawRuinThresholdIncreases(
                        amount = value.ruinThresholdIncreasesAmount,
                        increase = value.ruinThresholdIncreasesValue,
                    )
                )
            } else {
                current.ruinThresholdIncreases
            },
        )
        undefined
    }

}