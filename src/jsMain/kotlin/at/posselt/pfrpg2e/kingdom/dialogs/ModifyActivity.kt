package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.SkillInputArrayContext
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.SkillInputContext
import at.posselt.pfrpg2e.app.forms.SkillPicker
import at.posselt.pfrpg2e.app.forms.TextArea
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.data.actor.Proficiency
import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.activities.ActivityDcType
import at.posselt.pfrpg2e.data.kingdom.activities.getDcType
import at.posselt.pfrpg2e.kingdom.ActivityResult
import at.posselt.pfrpg2e.kingdom.RawActivity
import at.posselt.pfrpg2e.slugify
import at.posselt.pfrpg2e.utils.asSequence
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.core.utils.deepClone
import js.array.component1
import js.array.component2
import js.core.Void
import js.objects.unsafeJso
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

@JsPlainObject
external interface ModifyActivityContext : ValidatedHandlebarsContext {
    val formRows: Array<FormElementContext>
}

@JsPlainObject
external interface ModifyActivityData {
    val title: String
    val id: String
    val description: String
    val requirement: String?
    val special: String?
    val hint: String?
    val dcType: String
    val numericDc: Int
    val dcAdjustment: Int
    val phase: String
    val fortune: Boolean
    val oncePerRound: Boolean
    val criticalSuccess: String?
    val success: String?
    val failure: String?
    val criticalFailure: String?
    val actions: Int
}

@JsExport
class ModifyActivityDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            string("id")
            string("title")
            string("description")
            string("requirement", nullable = true)
            string("special", nullable = true)
            string("hint", nullable = true)
            string("criticalSuccess", nullable = true)
            string("success", nullable = true)
            string("failure", nullable = true)
            string("criticalFailure", nullable = true)
            int("actions")
            enum<ActivityDcType>("dcType")
            int("numericDc")
            boolean("fortune")
            boolean("oncePerRound")
            int("dcAdjustment", allowNegative = true)
            enum<KingdomPhase>("phase")
        }
    }
}


class ModifyActivity(
    data: RawActivity? = null,
    private val afterSubmit: suspend (data: RawActivity) -> Unit,
) : FormApp<ModifyActivityContext, ModifyActivityData>(
    title = if (data == null) t("kingdom.addActivity") else t("kingdom.editActivity"),
    template = "components/forms/application-form.hbs",
    debug = true,
    dataModel = ModifyActivityDataModel::class.js,
    id = "kmModifyActivity",
    width = 500,
) {
    private val edit: Boolean = data != null
    private var current: RawActivity = data?.let(::deepClone) ?: RawActivity(
        title = data?.title ?: "",
        id = data?.id ?: "",
        description = data?.description ?: "",
        requirement = data?.requirement,
        special = data?.special,
        phase = data?.phase ?: "leadership",
        dc = data?.dc ?: "control",//: KingdomDc
        dcAdjustment = data?.dcAdjustment,
        enabled = data?.enabled == true,
        automationNotes = data?.automationNotes,
        fortune = data?.fortune == true,
        oncePerRound = data?.oncePerRound == true,
        hint = data?.hint,
        skills = data?.skills ?: unsafeJso(),
        criticalSuccess = data?.criticalSuccess,
        success = data?.success,
        failure = data?.failure,
        criticalFailure = data?.criticalFailure,
        modifiers = data?.modifiers ?: emptyArray(),
        actions = data?.actions ?: 1,
        order = data?.order
    )

    init {
        isFormValid = data != null
    }

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "km-save" -> save()
            "edit-skills" -> KingdomSkillPicker(
                skillRanks = current.skills,
                onSave = {
                    current.skills = it
                    render()
                },
            ).launch()
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<ModifyActivityContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        ModifyActivityContext(
            partId = parent.partId,
            isFormValid = isFormValid,
            formRows = formContext(
                TextInput(
                    name = "id",
                    value = current.id,
                    label = t("applications.id"),
                    help = t("kingdom.overrideExistingElement"),
                    readonly = edit == true,
                    stacked = false,
                ),
                TextInput(
                    name = "title",
                    value = current.title,
                    label = t("applications.title"),
                    stacked = false,
                ),
                Select.range(
                    from = 1,
                    to = 3,
                    name = "actions",
                    label = t("kingdom.actions"),
                    value = current.actions ?: 1,
                    stacked = false,
                ),
                TextArea(
                    name = "description",
                    value = current.description,
                    label = t("applications.description"),
                    stacked = false,
                    help = t("kingdom.wrapTextInParagraph"),
                ),
                Select(
                    name = "phase",
                    value = current.phase,
                    label = t("kingdom.phase"),
                    required = false,
                    stacked = false,
                    options = KingdomPhase.entries
                        .filter { it != KingdomPhase.UPKEEP && it != KingdomPhase.EVENT }
                        .map { SelectOption(label = t(it), value = it.value) },
                ),
                TextInput(
                    name = "requirement",
                    value = current.requirement ?: "",
                    label = t("kingdom.requirement"),
                    required = false,
                    stacked = false,
                ),
                TextInput(
                    name = "special",
                    value = current.special ?: "",
                    label = t("kingdom.special"),
                    required = false,
                    stacked = false,
                ),
                TextInput(
                    name = "hint",
                    value = current.hint ?: "",
                    label = t("kingdom.hint"),
                    required = false,
                    stacked = false,
                ),
                CheckboxInput(
                    name = "fortune",
                    label = t("kingdom.fortune"),
                    value = current.fortune,
                    stacked = false,
                ),
                CheckboxInput(
                    name = "oncePerRound",
                    label = t("kingdom.oncePerRound"),
                    value = current.oncePerRound,
                    stacked = false,
                ),
                NumberInput(
                    name = "dcAdjustment",
                    value = current.dcAdjustment ?: 0,
                    label = t("kingdom.dcAdjustment"),
                    help = "Adds this value to the DC",
                    stacked = false,
                ),
                Select.fromEnum<ActivityDcType>(
                    name = "dcType",
                    value = getDcType(current.dc),
                    stacked = false,
                ),
                Select.dc(
                    name = "numericDc",
                    value = if (getDcType(current.dc) == ActivityDcType.VALUE) {
                        current.dc as Int
                    } else {
                        0
                    },
                    help = t("kingdom.numericDcHelp"),
                    stacked = false,
                ),
                SkillPicker(
                    context = SkillInputContext(
                        hideProficiency = false,
                        skills = current.skills.asSequence()
                            .mapNotNull { (skill, rank) ->
                                KingdomSkill.fromString(skill)?.let{
                                    SkillInputArrayContext(
                                        label = t(it),
                                        proficiency = t(Proficiency.fromRank(rank)),
                                    )
                                }
                            }
                            .toList()
                            .toTypedArray()
                    ),
                    stacked = false,
                ),
                TextArea(
                    name = "criticalSuccess",
                    value = current.criticalSuccess?.msg ?: "",
                    label = t("kingdom.criticalSuccessMessage"),
                    stacked = false,
                    required = false,
                    help = t("kingdom.wrapTextInParagraph"),
                ),
                TextArea(
                    name = "success",
                    value = current.success?.msg ?: "",
                    label = t("kingdom.successMessage"),
                    stacked = false,
                    required = false,
                    help = t("kingdom.wrapTextInParagraph"),
                ),
                TextArea(
                    name = "failure",
                    value = current.failure?.msg ?: "",
                    label = t("kingdom.failureMessage"),
                    stacked = false,
                    required = false,
                    help = t("kingdom.wrapTextInParagraph"),
                ),
                TextArea(
                    name = "criticalFailure",
                    value = current.criticalFailure?.msg ?: "",
                    label = t("kingdom.criticalFailureMessage"),
                    stacked = false,
                    required = false,
                    help = t("kingdom.wrapTextInParagraph"),
                ),
            )
        )
    }

    fun save(): Promise<Void> = buildPromise {
        if (isValid()) {
            close().await()
            afterSubmit(current)
        }
        undefined
    }

    override fun onParsedSubmit(value: ModifyActivityData): Promise<Void> = buildPromise {
        current = RawActivity(
            title = value.title,
            id = value.id.slugify(),
            description = value.description,
            requirement = value.requirement,
            special = value.special,
            hint = value.hint,
            dc = if (value.dcType == "value") {
                value.numericDc
            } else {
                value.dcType
            },
            dcAdjustment = value.dcAdjustment,
            phase = value.phase,
            actions = value.actions,
            fortune = value.fortune,
            oncePerRound = value.oncePerRound,
            criticalSuccess = value.criticalSuccess?.let {
                ActivityResult(
                    msg = it,
                    modifiers = current.criticalSuccess?.modifiers ?: emptyArray()
                )
            },
            success = value.success?.let {
                ActivityResult(
                    msg = it,
                    modifiers = current.success?.modifiers ?: emptyArray()
                )
            },
            failure = value.failure?.let {
                ActivityResult(
                    msg = it,
                    modifiers = current.failure?.modifiers ?: emptyArray()
                )
            },
            criticalFailure = value.criticalFailure?.let {
                ActivityResult(
                    msg = it,
                    modifiers = current.criticalFailure?.modifiers ?: emptyArray()
                )
            },
            skills = current.skills,
            modifiers = current.modifiers,
            enabled = true,
            order = current.order,
        )
        undefined
    }

}