package at.posselt.pfrpg2e.app

import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.OverrideType
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.camping.CampingSkill
import at.posselt.pfrpg2e.camping.DcType
import at.posselt.pfrpg2e.camping.ParsedCampingSkill
import at.posselt.pfrpg2e.data.actor.Lore
import at.posselt.pfrpg2e.data.actor.Perception
import at.posselt.pfrpg2e.data.actor.Proficiency
import at.posselt.pfrpg2e.data.actor.Skill
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.slugify
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.core.utils.deepClone
import js.core.Void
import js.objects.JsPlainObject
import kotlinx.coroutines.await
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

@JsPlainObject
external interface SkillInputArrayContext {
    val label: String
    val proficiency: String
}

@JsPlainObject
external interface PickerSkill {
    val label: String
    val name: String
    val enabled: Boolean
    val isLore: Boolean
    val proficiency: Proficiency
    val required: Boolean
    val validateOnly: Boolean
    val dcType: String
    val dc: Int?
}

@Suppress("unused")
@JsPlainObject
external interface SkillContext {
    val cells: Array<FormElementContext>
    val isLore: Boolean
    val index: Int
}

@Suppress("unused")
@JsPlainObject
external interface SkillPickerContext : ValidatedHandlebarsContext {
    val allowLores: Boolean
    val skills: Array<SkillContext>
    val anySkill: SkillContext?
    val atLeastOneSkillError: Boolean
    val anyEnabled: Boolean
}

@JsPlainObject
external interface SkillSubmitData {
    val label: String
    val name: String
    val enabled: Boolean
    val isLore: Boolean
    val dcType: String
    val dc: Int?
    val proficiency: String
    val required: Boolean
    val validateOnly: Boolean
}

external interface SkillPickerSubmitData {
    val skills: Array<SkillSubmitData>
}

@JsExport
class SkillPickerDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            array("skills") {
                schema {
                    string("label")
                    string("name")
                    boolean("enabled")
                    boolean("isLore")
                    string("dcType")
                    int("dc", nullable = true)
                    string("proficiency")
                    boolean("required")
                    boolean("validateOnly")
                }
            }
        }
    }
}

@JsPlainObject
external interface SkillPickerDcType {
    val value: String
    val label: String
}


@JsExport
class SkillPickerApplication(
    skills: Array<PickerSkill>,
    private val chooseOne: Boolean = false,
    private val allowLores: Boolean = false,
    private val dcTypes: Array<SkillPickerDcType>,
    private val afterSubmit: (skills: Array<PickerSkill>) -> Unit,
) : FormApp<SkillPickerContext, SkillPickerSubmitData>(
    title = if (chooseOne) t("applications.skillPicker.pickOneSkill") else t("applications.skillPicker.pickSkills"),
    template = "components/skill-picker/skill-picker.hbs",
    width = 1000,
    debug = true,
    classes = setOf("skill-picker"),
    dataModel = SkillPickerDataModel::class.js,
) {
    var currentSkills = deepClone(skills)
    var atLeastOneSkillError = false

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "km-save" -> buildPromise {
                if (isFormValid) {
                    val skills = currentSkills.find { it.name == "any" && it.enabled }?.let { arrayOf(it) }
                        ?: currentSkills.filter { it.enabled }.toTypedArray()
                    afterSubmit(skills)
                    close().await()
                }
            }

            "add-lore" -> buildPromise {
                val label = if (currentSkills.none { it.name == "new-lore" }) {
                    t("applications.skillPicker.newLore")
                } else {
                    t("applications.skillPicker.newLore") + " " + generateSequence(1) { it + 1 }
                        .dropWhile { index -> currentSkills.any { skill -> skill.name == "new-lore-$index" } }
                        .first()
                }
                currentSkills = currentSkills + PickerSkill(
                    label = label,
                    name = label.slugify(),
                    enabled = true,
                    isLore = true,
                    proficiency = Proficiency.UNTRAINED,
                    required = false,
                    validateOnly = false,
                    dcType = dcTypes.first().value,
                    dc = null,
                )
                validateAtLeastOnePresent(currentSkills)
                render()
            }

            "delete-lore" -> buildPromise {
                target.dataset["deleteIndex"]?.toInt()?.let { index ->
                    currentSkills = currentSkills.filterIndexed { idx, _ -> idx != index }.toTypedArray()
                    validateAtLeastOnePresent(currentSkills)
                    render()
                }
            }
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<SkillPickerContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val any = currentSkills.find { it.name == "any" }
        val anyEnabled = any?.enabled == true
        val anySkill = any?.let {
            toContext(currentSkills.indexOf(it), it, hideRequired = true, hideValidateOnly = true)
        }
        val skills = currentSkills
            .filter { it.name != "any" }
            .map { elem -> toContext(currentSkills.indexOf(elem), elem) }.toTypedArray()
        SkillPickerContext(
            partId = parent.partId,
            skills = skills,
            anySkill = anySkill,
            allowLores = allowLores,
            atLeastOneSkillError = atLeastOneSkillError,
            isFormValid = isFormValid,
            anyEnabled = anyEnabled,
        )
    }

    private fun toContext(
        index: Int,
        skill: PickerSkill,
        hideValidateOnly: Boolean = false,
        hideRequired: Boolean = false,
    ): SkillContext {
        val prefix = if (skill.isLore) {
            formContext(
                CheckboxInput(
                    label = skill.label,
                    name = "skills.$index.enabled",
                    value = skill.enabled,
                    hideLabel = true,
                    stacked = false,
                ),
                TextInput(
                    label = skill.label,
                    name = "skills.$index.label",
                    value = skill.label,
                    hideLabel = true,
                    stacked = false,
                ),
                HiddenInput(
                    name = "skills.$index.isLore",
                    value = "true",
                    overrideType = OverrideType.BOOLEAN,
                )
            )
        } else {
            formContext(
                CheckboxInput(
                    label = skill.label,
                    name = "skills.$index.enabled",
                    value = skill.enabled,
                    stacked = false,
                ),
                HiddenInput(
                    name = "skills.$index.label",
                    value = skill.label,
                ),
                HiddenInput(
                    label = skill.label,
                    name = "skills.$index.isLore",
                    value = "false",
                    overrideType = OverrideType.BOOLEAN,
                )
            )
        }
        return SkillContext(
            isLore = skill.isLore,
            index = index,
            cells = prefix + formContext(
                HiddenInput(
                    name = "skills.$index.name",
                    value = skill.name,
                ),
                Select.fromEnum<Proficiency>(
                    name = "skills.$index.proficiency",
                    value = skill.proficiency,
                    hideLabel = true,
                    elementClasses = listOf("km-proficiency"),
                    stacked = false,
                ),
                Select(
                    name = "skills.$index.dcType",
                    value = skill.dcType,
                    label = t("applications.skillPicker.dcType"),
                    stacked = false,
                    options = dcTypes.map { SelectOption(label = it.label, value = it.value) },
                ),
                Select.dc(
                    name = "skills.$index.dc",
                    required = false,
                    stacked = false,
                    value = if (skill.dcType == "static") skill.dc else null,
                    disabled = skill.dcType != "static",
                ),
                CheckboxInput(
                    label = t("applications.skillPicker.validateOnly"),
                    name = "skills.$index.validateOnly",
                    value = skill.validateOnly,
                    stacked = false,
                    hideLabel = hideValidateOnly,
                    elementClasses = if (hideValidateOnly) listOf("km-hidden") else emptyList(),
                ),
                CheckboxInput(
                    label = t("applications.skillPicker.required"),
                    name = "skills.$index.required",
                    value = skill.required,
                    stacked = false,
                    hideLabel = hideRequired,
                    elementClasses = if (hideRequired) listOf("km-hidden") else emptyList(),
                ),
            )
        )
    }

    override fun onParsedSubmit(value: SkillPickerSubmitData): Promise<Void> = buildPromise {
        currentSkills = value.skills
            .map {
                PickerSkill(
                    label = it.label,
                    name = if (it.isLore) it.label.slugify() else it.name,
                    enabled = it.enabled,
                    isLore = it.isLore,
                    proficiency = fromCamelCase<Proficiency>(it.proficiency) ?: Proficiency.UNTRAINED,
                    required = it.required,
                    validateOnly = it.validateOnly,
                    dcType = it.dcType,
                    dc = it.dc,
                )
            }
            .distinctBy { it.name }
            .toTypedArray()
        validateAtLeastOnePresent(currentSkills)
        undefined
    }

    private fun validateAtLeastOnePresent(value: Array<PickerSkill>) {
        if (chooseOne && value.none { it.enabled }) {
            isFormValid = false
            atLeastOneSkillError = true
        } else {
            atLeastOneSkillError = false
        }
    }

}

fun launchCampingSkillPicker(
    skills: List<ParsedCampingSkill>,
    afterSubmit: (Array<CampingSkill>) -> Unit,
) {
    val skillsByAttribute = skills.associateBy { it.attribute }
    val loreAttributes = skills.filter { it.attribute is Lore }.map { it.attribute }
    val anySkill = skills.find { it.attribute.value == "any" }?.let {
        PickerSkill(
            label = t("applications.any"),
            name = "any",
            enabled = true,
            isLore = false,
            proficiency = it.proficiency,
            required = false,
            validateOnly = false,
            dcType = it.dcType.toCamelCase(),
            dc = it.dc,
        )
    } ?: PickerSkill(
        label = t("applications.any"),
        name = "any",
        enabled = false,
        isLore = false,
        proficiency = Proficiency.UNTRAINED,
        required = false,
        validateOnly = false,
        dcType = "zone",
        dc = null,
    )
    val skills = (Skill.entries + Perception + loreAttributes).map { attribute ->
        val existingValue = skillsByAttribute[attribute]
        if (existingValue == null) {
            PickerSkill(
                label = t(attribute),
                name = attribute.value,
                enabled = false,
                isLore = attribute is Lore,
                proficiency = Proficiency.UNTRAINED,
                required = false,
                validateOnly = false,
                dcType = "zone",
                dc = null,
            )
        } else {
            PickerSkill(
                label = t(existingValue.attribute),
                name = existingValue.attribute.value,
                enabled = true,
                isLore = attribute is Lore,
                proficiency = existingValue.proficiency,
                required = existingValue.required,
                validateOnly = existingValue.validateOnly,
                dcType = existingValue.dcType.toCamelCase(),
                dc = existingValue.dc,
            )
        }
    }.toTypedArray()
    SkillPickerApplication(
        allowLores = true,
        chooseOne = false,
        skills = skills + anySkill,
        dcTypes = DcType.entries.map {
            SkillPickerDcType(value=it.value, label=t(it))
        }.toTypedArray(),
        afterSubmit = {
            afterSubmit(
                it.map {
                    CampingSkill(
                        name = it.name,
                        proficiency = it.proficiency.toCamelCase(),
                        dcType = it.dcType,
                        dc = it.dc,
                        validateOnly = it.validateOnly,
                        required = it.required,
                    )
                }.toTypedArray()
            )
        }
    ).launch()
}