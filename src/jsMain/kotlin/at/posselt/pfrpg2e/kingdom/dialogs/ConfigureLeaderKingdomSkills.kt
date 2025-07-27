package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.kingdom.RawLeaderKingdomSkills
import at.posselt.pfrpg2e.kingdom.hasSkill
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
import js.array.toTypedArray
import js.core.Void
import js.objects.Object
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

@Suppress("unused")
@JsPlainObject
external interface LeaderKingdomSkillsRow {
    val label: String
    val cells: Array<FormElementContext>
}

@Suppress("unused")
@JsPlainObject
external interface ConfigureLeaderKingdomSkillsContext : ValidatedHandlebarsContext {
    val headers: Array<String>
    val compact: Boolean
    val formRows: Array<LeaderKingdomSkillsRow>
    val saveLabel: String
}

@JsPlainObject
external interface ToggledKingdomSkills {
    val agriculture: Boolean
    val arts: Boolean
    val boating: Boolean
    val defense: Boolean
    val engineering: Boolean
    val exploration: Boolean
    val folklore: Boolean
    val industry: Boolean
    val intrigue: Boolean
    val magic: Boolean
    val politics: Boolean
    val scholarship: Boolean
    val statecraft: Boolean
    val trade: Boolean
    val warfare: Boolean
    val wilderness: Boolean
}

private fun ToggledKingdomSkills.toStringArray(): Array<String> =
    Object.entries(this).asSequence()
        .filter { (_, v) -> v == true }
        .map { (k, _) -> k }
        .toTypedArray()

@JsPlainObject
external interface LeaderKingdomSkillsData {
    val ruler: ToggledKingdomSkills
    val counselor: ToggledKingdomSkills
    val emissary: ToggledKingdomSkills
    val general: ToggledKingdomSkills
    val magister: ToggledKingdomSkills
    val treasurer: ToggledKingdomSkills
    val viceroy: ToggledKingdomSkills
    val warden: ToggledKingdomSkills
}

private fun LeaderKingdomSkillsData.toKingdomSkills(): RawLeaderKingdomSkills =
    RawLeaderKingdomSkills(
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
class ConfigureLeaderKingdomSkillsModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            Leader.entries.forEach { leader ->
                schema(leader.value) {
                    KingdomSkill.entries.forEach { skill ->
                        boolean(skill.value)
                    }
                }
            }
        }
    }
}

class ConfigureLeaderKingdomSkills(
    skills: RawLeaderKingdomSkills,
    private val readonly: Boolean,
    private val onSave: (skills: RawLeaderKingdomSkills) -> Unit,
) : FormApp<ConfigureLeaderKingdomSkillsContext, LeaderKingdomSkillsData>(
    title = "${t("kingdom.kingdomSkills")}${if (readonly) " (${t("kingdom.readonlySkills")})" else ""}",
    template = "components/forms/xy-form.hbs",
    debug = true,
    dataModel = ConfigureLeaderKingdomSkillsModel::class.js,
    id = "kmConfigureLeaderKingdomSkills",
) {
    var data = deepClone(skills)

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "km-save" -> {
                onSave(data)
                close()
            }
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<ConfigureLeaderKingdomSkillsContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val rows = KingdomSkill.entries
            .map { skill ->
                LeaderKingdomSkillsRow(
                    label = t(skill),
                    cells = Leader.entries
                        .map { leader ->
                            val name = leader.value + "." + skill.value
                            CheckboxInput(
                                name = name,
                                value = data.hasSkill(leader, skill),
                                label = name,
                                hideLabel = true,
                                disabled = readonly,
                            ).toContext()
                        }
                        .toTypedArray(),
                )
            }
            .toTypedArray()
        ConfigureLeaderKingdomSkillsContext(
            partId = parent.partId,
            headers = Leader.entries.map { t(it) }.toTypedArray(),
            formRows = rows,
            isFormValid = true,
            compact = true,
            saveLabel = if (readonly) t("applications.close") else t("applications.save"),
        )
    }

    override fun onParsedSubmit(value: LeaderKingdomSkillsData): Promise<Void> = buildPromise {
        if (readonly == false) {
            data = value.toKingdomSkills()
        }
        null
    }
}

fun configureLeaderKingdomSkills(
    skills: RawLeaderKingdomSkills,
    readonly: Boolean = false,
    onSave: (RawLeaderKingdomSkills) -> Unit,
) {
    ConfigureLeaderKingdomSkills(skills, readonly, onSave).launch()
}