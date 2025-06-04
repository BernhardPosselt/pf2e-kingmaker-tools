package at.posselt.pfrpg2e.app

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.utils.asSequence
import at.posselt.pfrpg2e.utils.buildPromise
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.core.utils.flattenObject
import js.array.toTypedArray
import js.core.Void
import js.objects.Record
import js.objects.unsafeJso
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

@JsPlainObject
external interface CrudColumn {
    val value: String
    val escapeHtml: Boolean
}


@JsPlainObject
external interface CrudItem {
    val id: String
    val name: String
    val nameIsHtml: Boolean
    val additionalColumns: Array<CrudColumn>
    val enable: FormElementContext
    val canBeEdited: Boolean
    val canBeDeleted: Boolean
}

@Suppress("unused")
@JsPlainObject
external interface CrudTemplateContext : ValidatedHandlebarsContext {
    val items: Array<CrudItem>
    val additionalColumnHeadings: Array<String>
}

@JsPlainObject
external interface CrudData {
    val enabledIds: Array<String>
}

class CrudApplicationDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            stringArray("enabledIds")
        }
    }
}


abstract class CrudApplication(
    title: String,
    id: String,
    width: Int? = undefined,
    debug: Boolean = false,
) : FormApp<CrudTemplateContext, CrudData>(
    title = title,
    template = "components/forms/crud-form.hbs",
    width = width,
    scrollable = setOf(".window-content"),
    debug = debug,
    id = id,
    dataModel = CrudApplicationDataModel::class.js,
) {
    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "delete" -> {
                val id = target.dataset["id"]
                checkNotNull(id)
                buildPromise {
                    deleteEntry(id)
                }
            }

            "add" -> {
                buildPromise {
                    addEntry()
                }
            }

            "edit" -> {
                val id = target.dataset["id"]
                checkNotNull(id)
                buildPromise {
                    editEntry(id)
                }

            }
        }
    }

    protected abstract fun addEntry(): Promise<Void>
    protected abstract fun deleteEntry(id: String): Promise<Void>
    protected abstract fun editEntry(id: String): Promise<Void>

    override fun fixObject(value: dynamic) {
        val ids = (value["enabledIds"] ?: unsafeJso()).unsafeCast<Record<String, Boolean>>()
        value["enabledIds"] = flattenObject(ids).asSequence()
            .filter { it.component2() == true }
            .map { it.component1() }
            .toTypedArray()
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<CrudTemplateContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val headings = getHeadings().await()
        val items = getItems().await()
        CrudTemplateContext(
            partId = parent.partId,
            additionalColumnHeadings = headings,
            items = items,
            isFormValid = isFormValid,
        )
    }

    protected abstract fun getItems(): Promise<Array<CrudItem>>
    protected abstract fun getHeadings(): Promise<Array<String>>
}