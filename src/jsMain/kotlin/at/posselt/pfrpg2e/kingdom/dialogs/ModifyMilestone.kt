package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.kingdom.RawMilestone
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
external interface ModifyMilestoneContext : ValidatedHandlebarsContext {
    val formRows: Array<FormElementContext>
}

@JsPlainObject
external interface ModifyMilestoneData {
    val name: String
    val id: String
    val xp: Int
}

@JsExport
class ModifyMilestoneDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
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
    title = if (data == null) t("kingdom.addMilestone") else t("kingdom.editMilestone"),
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
        isCultMilestone = data?.isCultMilestone == true,
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
    ): Promise<ModifyMilestoneContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        ModifyMilestoneContext(
            partId = parent.partId,
            isFormValid = isFormValid,
            formRows = formContext(
                TextInput(
                    name = "id",
                    value = current.id,
                    label = t("applications.id"),
                    help = t("kingdom.overrideExistingElement"),
                    readonly = edit == true,
                ),
                TextInput(
                    name = "name",
                    value = current.name,
                    label = t("applications.name"),
                ),
                NumberInput(
                    name = "xp",
                    value = current.xp,
                    label = t("applications.xp")
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
            id = value.id.slugify(),
            xp = value.xp,
            completed = current.completed,
            enabledOnFirstRun = current.enabledOnFirstRun,
            isCultMilestone = current.isCultMilestone,
        )
        undefined
    }

}