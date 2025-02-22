package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.Leader
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.LeaderKingdomSkills
import at.posselt.pfrpg2e.kingdom.knowsSkill
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.launch
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import js.array.toTypedArray
import js.core.Void
import js.objects.Object
import kotlinx.coroutines.await
import kotlinx.coroutines.coroutineScope
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

private fun ToggledSkills.toStringArray(): Array<String> =
    Object.entries(this).asSequence()
        .filter { (_, v) -> v == true }
        .map {  (k, _) -> k }
        .toTypedArray()

@JsPlainObject
private external interface LeaderKingdomSkillsData {
    val ruler: ToggledSkills
    val counselor: ToggledSkills
    val emissary: ToggledSkills
    val general: ToggledSkills
    val magister: ToggledSkills
    val treasurer: ToggledSkills
    val viceroy: ToggledSkills
    val warden: ToggledSkills
}

private fun LeaderKingdomSkillsData.toKingdomSkills(): LeaderKingdomSkills =
    LeaderKingdomSkills(
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
class ConfigureLeaderKingdomSkillsModel(val value: AnyObject) : DataModel(value) {
    companion object {
        @Suppress("unused")
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

private class ConfigureLeaderKingdomSkills(
    val kingdom: KingdomData,
//    private val afterSubmit: () -> Unit,
) : FormApp<ConfigureLeaderSkillsContext, LeaderKingdomSkillsData>(
    title = "Configure Leader Kingdom Skills ",
    template = "components/forms/xy-form.hbs",
    debug = true,
    dataModel = ConfigureLeaderKingdomSkillsModel::class.js,
    id = "kmConfigureKingdomSkills",
) {
    var data = kingdom.leaderKingdomSkills

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "save" -> save()
        }
    }

    fun save() {
        console.log(data)
        close()
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
                    cells = KingdomSkill.entries
                        .map { skill ->
                            val name = leader.value + "." + skill.value
                            LeaderSkillsCell(
                                input = CheckboxInput(
                                    name = name,
                                    value = data.knowsSkill(leader, skill),
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
            headers = KingdomSkill.entries.map { it.label }.toTypedArray(),
            formRows = rows,
            isFormValid = true,
        )
    }

    override fun onParsedSubmit(value: LeaderKingdomSkillsData): Promise<Void> = buildPromise {
        data = value.toKingdomSkills()
        null
    }
}

suspend fun configureLeaderKingdomSkills(
    kingdom: KingdomData,
): Unit = coroutineScope {
    ConfigureLeaderKingdomSkills(kingdom).launch()
}