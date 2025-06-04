package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.data.actor.Proficiency
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.t
import at.posselt.pfrpg2e.utils.toMutableRecord
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import js.core.Void
import js.objects.Record
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.Array
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.arrayOf
import kotlin.collections.filter
import kotlin.collections.map
import kotlin.collections.mapIndexed
import kotlin.collections.toTypedArray
import kotlin.js.Promise
import kotlin.let
import kotlin.to


@Suppress("unused")
@JsPlainObject
external interface KingdomSkillPickerRow {
    val label: String
    val cells: Array<FormElementContext>
}

@Suppress("unused")
@JsPlainObject
external interface KingdomSkillPickerContext : ValidatedHandlebarsContext {
    val headers: Array<String>
    val formRows: Array<KingdomSkillPickerRow>
}

@JsPlainObject
external interface KingdomSkillPickerSkillData {
    val enabled: Boolean
    val skill: String
    val proficiency: String
}

@JsPlainObject
external interface KingdomSkillPickerData {
    val skills: Array<KingdomSkillPickerSkillData>
}

class KingdomSkillPickerModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            array("skills") {
                schema {
                    boolean("enabled")
                    string("skill")
                    enum<Proficiency>("proficiency")
                }
            }
        }
    }
}

class KingdomSkillPicker(
    private val skillRanks: Record<String, Int>,
    private val onSave: (ranks: Record<String, Int>) -> Unit,
    private val includeProficiency: Boolean = true,
    private val requireAtLeastOneSkill: Boolean = false,
) : FormApp<KingdomSkillPickerContext, KingdomSkillPickerData>(
    title = t("kingdom.pickSkills"),
    template = "components/forms/xy-form.hbs",
    debug = true,
    dataModel = KingdomSkillPickerModel::class.js,
    id = "kmKingdomSkillPicker",
) {
    var data = KingdomSkillPickerData(
        skills = KingdomSkill.entries
            .map {
                val value = skillRanks[it.value]
                KingdomSkillPickerSkillData(
                    enabled = value != null,
                    skill = it.value,
                    proficiency = value?.let { rank -> Proficiency.fromRank(rank) }?.value
                        ?: Proficiency.UNTRAINED.value,
                )
            }
            .toTypedArray()
    )

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "km-save" -> {
                val ranks = data.skills.filter { it.enabled }
                    .map { it.skill to (Proficiency.fromString(it.proficiency)?.rank ?: 0) }
                    .toMutableRecord()
                onSave(ranks)
                close()
            }
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<KingdomSkillPickerContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()

        val rows = data.skills.mapIndexed { index, skill ->
            val label = KingdomSkill.fromString(skill.skill) ?: KingdomSkill.AGRICULTURE
            KingdomSkillPickerRow(
                label = t(label),
                cells = formContext(
                    HiddenInput(
                        name = "skills.$index.skill",
                        label = t("applications.skill"),
                        value = skill.skill,
                    ),
                    CheckboxInput(
                        name = "skills.$index.enabled",
                        label = t("applications.enable"),
                        hideLabel = true,
                        value = skill.enabled,
                    ),
                    if (includeProficiency) {
                        Select.fromEnum<Proficiency>(
                            name = "skills.$index.proficiency",
                            value = fromCamelCase<Proficiency>(skill.proficiency) ?: Proficiency.UNTRAINED,
                            hideLabel = true,
                        )
                    } else {
                        HiddenInput(
                            name = "skills.$index.proficiency",
                            value = Proficiency.UNTRAINED.value,
                        )
                    },
                )
            )
        }.toTypedArray()
        KingdomSkillPickerContext(
            partId = parent.partId,
            formRows = rows,
            isFormValid = isFormValid,
            headers = if (includeProficiency) arrayOf("", t("kingdom.allowSkill"), t("kingdom.minimumProficiency")) else arrayOf(
                "",
                t("kingdom.allowSkill")
            )
        )
    }

    override fun onParsedSubmit(value: KingdomSkillPickerData): Promise<Void> = buildPromise {
        if (requireAtLeastOneSkill && value.skills.none { it.enabled }) {
            isFormValid = false
        }
        data = value
        null
    }
}