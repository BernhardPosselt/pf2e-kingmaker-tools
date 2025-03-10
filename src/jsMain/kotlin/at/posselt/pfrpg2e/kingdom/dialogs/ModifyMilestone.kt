package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.kingdom.RawMilestone
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
external interface ModifyMilestoneContext : HandlebarsRenderContext {
    val isFormValid: Boolean
    val formRows: Array<FormElementContext>
}

@JsPlainObject
external interface ModifyMilestoneData {
    val name: String
    val id: String
    val xp: Int
}

@JsExport
class ModifyMilestoneDataModel(value: AnyObject) : DataModel(value) {
    companion object {
        @Suppress("unused")
        @JsStatic
        fun defineSchema() = buildSchema {
            string("id")
            string("name")
            int("xp")
        }
    }
}


class ModifyMilestone(
    data: RawMilestone? = null,
    private val afterSubmit: suspend (data: RawMilestone) -> Unit,
) : FormApp<ModifyMilestoneContext, ModifyMilestoneData>(
    title = if (data == null) "Add Milestone" else "Edit Milestone",
    template = "components/forms/application-form.hbs",
    debug = true,
    dataModel = ModifyMilestoneDataModel::class.js,
    id = "kmModifyMilestone"
) {
    private val edit: Boolean = data != null
    private var current: RawMilestone = data?.let(::deepClone) ?: RawMilestone(
        name = data?.name ?: "",
        id = data?.id ?: "",
        xp = data?.xp ?: 0,
        completed = data?.completed == true,
        enabledOnFirstRun = data?.enabledOnFirstRun == true,
        isCultEvent = data?.isCultEvent == true,
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
    ): Promise<ModifyMilestoneContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        ModifyMilestoneContext(
            partId = parent.partId,
            isFormValid = isFormValid,
            formRows = formContext(
                TextInput(
                    name = "id",
                    value = current.id,
                    label = "Id",
                    help = "Choose the same Id as an existing milestone to override it",
                    readonly = edit == true,
                ),
                TextInput(
                    name = "name",
                    value = current.name,
                    label = "Name",
                ),
                NumberInput(
                    name = "xp",
                    value = current.xp,
                    label = "XP"
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

    override fun onParsedSubmit(value: ModifyMilestoneData): Promise<Void> = buildPromise {
        current = RawMilestone(
            name = value.name,
            id = value.id,
            xp = value.xp,
            completed = current.completed,
            enabledOnFirstRun = current.enabledOnFirstRun,
            isCultEvent = current.isCultEvent,
        )
        undefined
    }

}