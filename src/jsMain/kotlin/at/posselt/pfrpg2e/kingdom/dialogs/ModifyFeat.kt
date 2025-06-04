package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
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
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
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
external interface ModifyFeatContext : ValidatedHandlebarsContext {
    val formRows: Array<FormElementContext>
}

@JsPlainObject
external interface ModifyFeatData {
    val name: String
    val id: String
    val text: String
    val level: Int
    val resourceDice: Int
    val settlementMagicItemLevelIncrease: Int
    val rollOption: String?
    val increaseAnarchyLimit: Int
    val ruinThresholdIncreasesAmount: Int
    val ruinThresholdIncreasesValue: Int
}

class ModifyFeatDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            string("id")
            string("name")
            string("text")
            int("level")
            int("resourceDice")
            int("settlementMagicItemLevelIncrease")
            string("rollOption", nullable = true)
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
    title = if (data == null) t("kingdom.addFeat") else t("kingdom.editFeat"),
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
        automationNotes = data?.automationNotes,
        modifiers = data?.modifiers,
        resourceDice = data?.resourceDice,
        requirements = data?.requirements,
        settlementMagicItemLevelIncrease = data?.settlementMagicItemLevelIncrease,
        trainSkill = data?.trainSkill,
        assuranceForSkill = data?.assuranceForSkill,
        increaseUsableSkills = data?.increaseUsableSkills,
        rollOptions = data?.rollOptions,
        increaseAnarchyLimit = data?.increaseAnarchyLimit,
        ruinThresholdIncreases = data?.ruinThresholdIncreases,
        increaseGainedLuxuriesOncePerTurnBy = data?.increaseGainedLuxuriesOncePerTurnBy,
        increaseActivityUnrestReductionBy = data?.increaseActivityUnrestReductionBy,
    )

    init {
        isFormValid = data != null
    }

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "km-save" -> save()
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
                    label = t("applications.id"),
                    help = t("kingdom.overrideExistingElement"),
                    readonly = edit == true,
                    stacked = false
                ),
                TextInput(
                    name = "name",
                    value = current.name,
                    label = t("applications.name"),
                    stacked = false
                ),
                TextArea(
                    name = "text",
                    value = current.text,
                    label = t("applications.description"),
                    stacked = false
                ),
                Select.range(
                    from = 1,
                    to = 20,
                    name = "level",
                    value = current.level,
                    label = t("applications.level"),
                    stacked = false
                ),
                NumberInput(
                    name = "resourceDice",
                    value = current.resourceDice ?: 0,
                    label = t("kingdom.bonusResourceDice"),
                    stacked = false
                ),
                NumberInput(
                    name = "settlementMagicItemLevelIncrease",
                    value = current.settlementMagicItemLevelIncrease ?: 0,
                    label = t("kingdom.settlementMagicItemLevelIncrease"),
                    stacked = false
                ),
                NumberInput(
                    name = "increaseAnarchyLimit",
                    value = current.increaseAnarchyLimit ?: 0,
                    label = t("kingdom.increaseAnarchyLimit"),
                    stacked = false
                ),
                NumberInput(
                    name = "ruinThresholdIncreasesAmount",
                    value = current.ruinThresholdIncreases?.firstOrNull()?.amount ?:0,
                    label = t("kingdom.ruinThresholdIncreases"),
                    help = t("kingdom.ruinThresholdIncreasesHelp"),
                    stacked = false,
                ),
                NumberInput(
                    name = "ruinThresholdIncreasesValue",
                    value = current.ruinThresholdIncreases?.firstOrNull()?.increase ?:0,
                    label = t("kingdom.ruinThresholdIncreasesValue"),
                    help = t("kingdom.ruinThresholdIncreasesValueHelp"),
                    stacked = false
                ),
                TextInput(
                    name = "rollOption",
                    value = current.rollOptions?.firstOrNull() ?: "",
                    label = t("kingdom.rollOption"),
                    stacked = false,
                    required = false,
                    help = t("kingdom.rollOptionHelp")
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
            automationNotes = current.automationNotes,
            modifiers = current.modifiers,
            resourceDice = value.resourceDice,
            settlementMagicItemLevelIncrease = value.settlementMagicItemLevelIncrease,
            trainSkill = current.trainSkill,
            assuranceForSkill = current.assuranceForSkill,
            increaseUsableSkills = current.increaseUsableSkills,
            increaseGainedLuxuriesOncePerTurnBy = current.increaseGainedLuxuriesOncePerTurnBy,
            increaseActivityUnrestReductionBy = current.increaseActivityUnrestReductionBy,
            rollOptions = value.rollOption?.takeIf { it.isNotEmpty() }?.let { arrayOf(it) } ?: emptyArray(),
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