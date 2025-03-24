package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.TextArea
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.data.kingdom.KingdomAbility
import at.posselt.pfrpg2e.kingdom.RawHeartland
import at.posselt.pfrpg2e.slugify
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
external interface ModifyHeartlandContext : ValidatedHandlebarsContext {
    val formRows: Array<FormElementContext>
}

@JsPlainObject
external interface ModifyHeartlandData {
    val name: String
    val id: String
    val description: String
    val boost: String
}

@JsExport
class ModifyHeartlandDataModel(value: AnyObject) : DataModel(value) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            string("id")
            string("name")
            string("description")
            enum<KingdomAbility>("boost")
        }
    }
}


class ModifyHeartland(
    data: RawHeartland? = null,
    private val afterSubmit: suspend (data: RawHeartland) -> Unit,
) : FormApp<ModifyHeartlandContext, ModifyHeartlandData>(
    title = if (data == null) "Add Heartland" else "Edit Heartland",
    template = "components/forms/application-form.hbs",
    debug = true,
    dataModel = ModifyHeartlandDataModel::class.js,
    id = "kmModifyHeartland"
) {
    private val edit: Boolean = data != null
    private var current: RawHeartland = data?.let(::deepClone) ?: RawHeartland(
        name = data?.name ?: "",
        id = data?.id ?:"",
        description = data?.description ?:"",
        boost = data?.boost ?:"culture",
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
    ): Promise<ModifyHeartlandContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        ModifyHeartlandContext(
            partId = parent.partId,
            isFormValid = isFormValid,
            formRows = formContext(
                TextInput(
                    name = "id",
                    value = current.id,
                    label = "Id",
                    help = "Choose the same Id as an existing heartland to override it",
                    readonly = edit == true,
                ),
                TextInput(
                    name = "name",
                    value = current.name,
                    label = "Name",
                ),
                TextArea(
                    name = "description",
                    value = current.description,
                    label = "Description",
                ),
                Select.fromEnum<KingdomAbility>(
                    name = "boost",
                    value = KingdomAbility.fromString(current.boost) ?: KingdomAbility.CULTURE,
                    label = "Boost",
                )
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

    override fun onParsedSubmit(value: ModifyHeartlandData): Promise<Void> = buildPromise {
        current = RawHeartland(
            name = value.name,
            id = value.id.slugify(),
            boost = value.boost,
            description = value.description,
        )
        undefined
    }

}