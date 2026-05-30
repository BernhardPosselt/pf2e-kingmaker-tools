package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.kingdom.data.RawQuest
import at.posselt.pfrpg2e.kingdom.data.RawQuestRewards
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import js.core.Void
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

@JsExport
class QuestModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            string("title")
            string("description")
            string("giver")
            string("type")
            string("target", nullable = true)
            int("rp")
            int("xp")
            int("unrest", allowNegative = true)
            int("food")
            int("lumber")
            int("stone")
            int("ore")
            int("luxuries")
            string("flavorTextCompleted")
        }
    }
}

@JsPlainObject
external interface AddQuestData {
    val title: String
    val description: String
    val giver: String
    val type: String
    val target: String?
    val rp: Int
    val xp: Int
    val unrest: Int
    val food: Int
    val lumber: Int
    val stone: Int
    val ore: Int
    val luxuries: Int
    val flavorTextCompleted: String
}

@JsPlainObject
external interface AddQuestContext : ValidatedHandlebarsContext {
    val formRows: Array<FormElementContext>
}

class AddQuest(
    private val onSave: suspend (quest: RawQuest) -> Unit,
) : FormApp<AddQuestContext, AddQuestData>(
    title = t("kingdom.quests.addQuest"),
    template = "components/forms/application-form.hbs",
    debug = true,
    dataModel = QuestModel::class.js,
    id = "kmAddQuest",
) {
    var data: AddQuestData = AddQuestData(
        title = "",
        description = "",
        giver = "",
        type = "other",
        target = null,
        rp = 0,
        xp = 0,
        unrest = 0,
        food = 0,
        lumber = 0,
        stone = 0,
        ore = 0,
        luxuries = 0,
        flavorTextCompleted = "",
    )

    init {
        isFormValid = false
    }

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "km-save" -> {
                val quest = RawQuest(
                    id = "quest-${js("Date.now()")}",
                    title = data.title,
                    description = data.description,
                    giver = data.giver,
                    status = "active",
                    type = data.type,
                    target = data.target,
                    rewards = RawQuestRewards(
                        rp = if (data.rp != 0) data.rp else null,
                        xp = if (data.xp != 0) data.xp else null,
                        unrest = if (data.unrest != 0) data.unrest else null,
                        food = if (data.food != 0) data.food else null,
                        lumber = if (data.lumber != 0) data.lumber else null,
                        stone = if (data.stone != 0) data.stone else null,
                        ore = if (data.ore != 0) data.ore else null,
                        luxuries = if (data.luxuries != 0) data.luxuries else null,
                    ),
                    flavorTextCompleted = data.flavorTextCompleted,
                )
                buildPromise {
                    onSave(quest)
                    close()
                }
            }
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<AddQuestContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val typeOptions = listOf(
            SelectOption(value = "explore_hex", label = t("kingdom.quests.type.explore_hex")),
            SelectOption(value = "claim_hex", label = t("kingdom.quests.type.claim_hex")),
            SelectOption(value = "build_structure", label = t("kingdom.quests.type.build_structure")),
            SelectOption(value = "clear_hex", label = t("kingdom.quests.type.clear_hex")),
            SelectOption(value = "assign_leader", label = t("kingdom.quests.type.assign_leader")),
            SelectOption(value = "other", label = t("kingdom.quests.type.other")),
        )
        val rows = formContext(
            TextInput(
                name = "title",
                label = t("kingdom.quests.fields.title"),
                stacked = false,
                value = data.title,
            ),
            TextInput(
                name = "giver",
                label = t("kingdom.quests.fields.giver"),
                stacked = false,
                value = data.giver,
            ),
            Select(
                name = "type",
                label = t("kingdom.quests.fields.type"),
                value = data.type,
                options = typeOptions,
                stacked = false,
            ),
            TextInput(
                name = "target",
                label = t("kingdom.quests.fields.target"),
                stacked = false,
                value = data.target ?: "",
                required = false,
            ),
            TextInput(
                name = "description",
                label = t("kingdom.quests.fields.description"),
                stacked = false,
                value = data.description,
            ),
            TextInput(
                name = "flavorTextCompleted",
                label = t("kingdom.quests.fields.flavorTextCompleted"),
                stacked = false,
                value = data.flavorTextCompleted,
            ),
            NumberInput(
                name = "rp",
                label = t("kingdom.quests.fields.rp"),
                value = data.rp,
                stacked = false,
                required = false,
            ),
            NumberInput(
                name = "xp",
                label = t("kingdom.quests.fields.xp"),
                value = data.xp,
                stacked = false,
                required = false,
            ),
            NumberInput(
                name = "unrest",
                label = t("kingdom.quests.fields.unrest"),
                value = data.unrest,
                stacked = false,
                required = false,
            ),
            NumberInput(
                name = "food",
                label = t("kingdom.quests.fields.food"),
                value = data.food,
                stacked = false,
                required = false,
            ),
            NumberInput(
                name = "lumber",
                label = t("kingdom.quests.fields.lumber"),
                value = data.lumber,
                stacked = false,
                required = false,
            ),
            NumberInput(
                name = "stone",
                label = t("kingdom.quests.fields.stone"),
                value = data.stone,
                stacked = false,
                required = false,
            ),
            NumberInput(
                name = "ore",
                label = t("kingdom.quests.fields.ore"),
                value = data.ore,
                stacked = false,
                required = false,
            ),
            NumberInput(
                name = "luxuries",
                label = t("kingdom.quests.fields.luxuries"),
                value = data.luxuries,
                stacked = false,
                required = false,
            ),
        )
        AddQuestContext(
            partId = parent.partId,
            formRows = rows,
            isFormValid = isFormValid,
        )
    }

    override fun onParsedSubmit(value: AddQuestData): Promise<Void> = buildPromise {
        data = value
        null
    }
}
