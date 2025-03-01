package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.forms.Button
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.DataAttribute
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.prompt
import at.posselt.pfrpg2e.data.actor.Attribute
import at.posselt.pfrpg2e.data.actor.Lore
import at.posselt.pfrpg2e.data.actor.Skill
import at.posselt.pfrpg2e.data.kingdom.Leader
import at.posselt.pfrpg2e.kingdom.RawLeaderSkills
import at.posselt.pfrpg2e.kingdom.deleteLore
import at.posselt.pfrpg2e.kingdom.hasAttribute
import at.posselt.pfrpg2e.slugify
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.launch
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.core.ui
import com.foundryvtt.core.utils.deepClone
import js.array.toTypedArray
import js.core.Void
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise


@JsPlainObject
private external interface AddEntryContext {
    val formRows: Array<FormElementContext>
}

@JsPlainObject
private external interface AddEntryData {
    val lore: String
}


@JsPlainObject
private external interface LeaderSkillsRow {
    val label: String
    val cells: Array<FormElementContext>
}

@JsPlainObject
private external interface ConfigureLeaderSkillsContext : HandlebarsRenderContext {
    val headers: Array<String>
    val isFormValid: Boolean
    val formRows: Array<LeaderSkillsRow>
    val compact: Boolean
    val addEntry: String
}

private fun filterLores(values: Array<String>): Array<Attribute> =
    values
        .asSequence()
        .map { Attribute.fromString(it) }
        .filter { it is Lore }
        .toTypedArray()

@JsPlainObject
private external interface LeaderSkillsData {
    val ruler: Array<Boolean>
    val counselor: Array<Boolean>
    val emissary: Array<Boolean>
    val general: Array<Boolean>
    val magister: Array<Boolean>
    val treasurer: Array<Boolean>
    val viceroy: Array<Boolean>
    val warden: Array<Boolean>
}

private fun RawLeaderSkills.allLores(): Array<Attribute> =
    filterLores(ruler) +
            filterLores(counselor) +
            filterLores(emissary) +
            filterLores(general) +
            filterLores(magister) +
            filterLores(treasurer) +
            filterLores(viceroy) +
            filterLores(warden)

private fun toAttributeValues(toggles: Array<Boolean>, attributes: Array<Attribute>): Array<String> =
    attributes
        .mapIndexed { idx, attr -> if (toggles[idx] == true) attr.value else null }
        .filterNotNull()
        .toTypedArray()

private fun LeaderSkillsData.toSkills(attributes: Array<Attribute>): RawLeaderSkills =
    RawLeaderSkills(
        ruler = toAttributeValues(ruler, attributes),
        counselor = toAttributeValues(counselor, attributes),
        emissary = toAttributeValues(emissary, attributes),
        general = toAttributeValues(general, attributes),
        magister = toAttributeValues(magister, attributes),
        treasurer = toAttributeValues(treasurer, attributes),
        viceroy = toAttributeValues(viceroy, attributes),
        warden = toAttributeValues(warden, attributes),
    )

@JsExport
class ConfigureLeaderSkillsModel(val value: AnyObject) : DataModel(value) {
    companion object {
        @Suppress("unused")
        @JsStatic
        fun defineSchema() = buildSchema {
            Leader.entries.forEach { leader ->
                booleanArray(leader.value)
            }
        }
    }
}

private class ConfigureLeaderSkills(
    skills: RawLeaderSkills,
    private val onSave: (skills: RawLeaderSkills) -> Unit,
) : FormApp<ConfigureLeaderSkillsContext, LeaderSkillsData>(
    title = "Configure Leader Skills ",
    template = "components/forms/xy-form.hbs",
    debug = true,
    dataModel = ConfigureLeaderSkillsModel::class.js,
    id = "kmConfigureLeaderSkills",
) {
    var data = deepClone(skills)
    var lores = data.allLores()

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "save" -> {
                onSave(data)
                close()
            }

            "delete-lore" -> {
                target.dataset["lore"]?.let { index ->
                    lores.find { it.value == index }
                        ?.let { data = data.deleteLore(it) }
                    lores = lores.filter { it.value != index }.toTypedArray()
                    render()
                }
            }

            "add-entry" -> {
                buildPromise {
                    prompt<AddEntryData, Unit>(
                        title = "Add Lore",
                        templatePath = "components/forms/form.hbs",
                        templateContext = AddEntryContext(
                            formRows = arrayOf(
                                TextInput(
                                    label = "Lore",
                                    name = "lore",
                                    value = "",
                                ).toContext()
                            )
                        ).unsafeCast<AnyObject>()
                    ) { data ->
                        val lore = Attribute.fromString(data.lore.slugify())
                        if (data.lore.isBlank() || lore is Skill) {
                            ui.notifications.error("Not a valid lore")
                        } else if (lores.contains(lore)) {
                            ui.notifications.error("Lore exists already")
                        } else {
                            lores = lores + lore
                            render()
                        }
                    }
                }
            }
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<ConfigureLeaderSkillsContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val rows = (Skill.entries + lores)
            .mapIndexed { attributeIndex, attribute ->
                val deleteButton = Button(
                    value = "delete-lore",
                    label = "",
                    icon = "fa-solid fa-trash",
                    data = listOf(DataAttribute(key = "lore", value = attribute.value)),
                    disabled = attribute !is Lore,
                )
                LeaderSkillsRow(
                    label = attribute.label,
                    cells = Leader.entries
                        .map { leader ->
                            val name = leader.value + "." + attributeIndex
                            CheckboxInput(
                                name = name,
                                value = data.hasAttribute(leader, attribute),
                                label = name,
                                hideLabel = true,
                            ).toContext()
                        }
                        .toTypedArray() + deleteButton.toContext(),
                )
            }
            .toTypedArray()
        ConfigureLeaderSkillsContext(
            partId = parent.partId,
            headers = Leader.entries.map { it.label }.toTypedArray() + "Delete",
            formRows = rows,
            isFormValid = true,
            compact = true,
            addEntry = "Add Lore",
        )
    }

    override fun onParsedSubmit(value: LeaderSkillsData): Promise<Void> = buildPromise {
        data = value.toSkills(Skill.entries.toTypedArray() + lores)
        null
    }
}

fun configureLeaderSkills(
    skills: RawLeaderSkills,
    onSave: (RawLeaderSkills) -> Unit,
) {
    ConfigureLeaderSkills(skills, onSave).launch()
}