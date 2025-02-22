package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.data.actor.Skill
import at.posselt.pfrpg2e.data.kingdom.Leader
import at.posselt.pfrpg2e.kingdom.LeaderSkills
import at.posselt.pfrpg2e.kingdom.hasAttribute
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.launch
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.core.utils.deepClone
import js.array.toTypedArray
import js.core.Void
import js.objects.Object
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise


@JsPlainObject
private external interface LeaderSkillsCell {
    val input: FormElementContext
}


@JsPlainObject
private external interface LeaderSkillsRow {
    val label: String
    val cells: Array<LeaderSkillsCell>
}

@JsPlainObject
private external interface ConfigureLeaderSkillsContext : HandlebarsRenderContext {
    val headers: Array<String>
    val isFormValid: Boolean
    val formRows: Array<LeaderSkillsRow>
}

@JsPlainObject
private external interface ToggledSkills {
    val acrobatics: Boolean
    val arcana: Boolean
    val athletics: Boolean
    val crafting: Boolean
    val deception: Boolean
    val diplomacy: Boolean
    val intimidation: Boolean
    val medicine: Boolean
    val nature: Boolean
    val occultism: Boolean
    val performance: Boolean
    val religion: Boolean
    val society: Boolean
    val stealth: Boolean
    val survival: Boolean
    val thievery: Boolean
}

private fun ToggledSkills.toStringArray(): Array<String> =
    Object.entries(this).asSequence()
        .filter { (_, v) -> v == true }
        .map { (k, _) -> k }
        .toTypedArray()

@JsPlainObject
private external interface LeaderSkillsData {
    val ruler: ToggledSkills
    val counselor: ToggledSkills
    val emissary: ToggledSkills
    val general: ToggledSkills
    val magister: ToggledSkills
    val treasurer: ToggledSkills
    val viceroy: ToggledSkills
    val warden: ToggledSkills
}

private fun LeaderSkillsData.toSkills(): LeaderSkills =
    LeaderSkills(
        ruler = ruler.toStringArray(),
        counselor = counselor.toStringArray(),
        emissary = emissary.toStringArray(),
        general = general.toStringArray(),
        magister = magister.toStringArray(),
        treasurer = treasurer.toStringArray(),
        viceroy = viceroy.toStringArray(),
        warden = warden.toStringArray(),
    )

@JsExport
class ConfigureLeaderSkillsModel(val value: AnyObject) : DataModel(value) {
    companion object {
        @Suppress("unused")
        @JsStatic
        fun defineSchema() = buildSchema {
            Leader.entries.forEach { leader ->
                schema(leader.value) {
                    Skill.entries.forEach { skill ->
                        boolean(skill.value)
                    }
                }
            }
        }
    }
}

private class ConfigureLeaderSkills(
    skills: LeaderSkills,
    private val onSave: (skills: LeaderSkills) -> Unit,
) : FormApp<ConfigureLeaderSkillsContext, LeaderSkillsData>(
    title = "Configure Leader Skills ",
    template = "components/forms/xy-form.hbs",
    debug = true,
    dataModel = ConfigureLeaderSkillsModel::class.js,
    id = "kmConfigureLeaderSkills",
) {
    var data = deepClone(skills)

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "save" -> {
                onSave(data)
                close()
            }
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<ConfigureLeaderSkillsContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val rows = Leader.entries
            .map { leader ->
                LeaderSkillsRow(
                    label = leader.label,
                    // TODO: add button to configure lores
                    cells = Skill.entries
                        .map { attribute ->
                            val name = leader.value + "." + attribute.value
                            LeaderSkillsCell(
                                input = CheckboxInput(
                                    name = name,
                                    value = data.hasAttribute(leader, attribute),
                                    label = name,
                                    hideLabel = true,
                                ).toContext()
                            )
                        }
                        .toTypedArray(),
                )
            }
            .toTypedArray()
        ConfigureLeaderSkillsContext(
            partId = parent.partId,
            headers = Skill.entries.map { it.label }.toTypedArray() + "Lores",
            formRows = rows,
            isFormValid = true,
        )
    }

    override fun onParsedSubmit(value: LeaderSkillsData): Promise<Void> = buildPromise {
        // TODO: merge with lores
        data = value.toSkills()
        null
    }
}

fun configureLeaderSkills(
    skills: LeaderSkills,
    onSave: (LeaderSkills) -> Unit,
) {
    ConfigureLeaderSkills(skills, onSave).launch()
}