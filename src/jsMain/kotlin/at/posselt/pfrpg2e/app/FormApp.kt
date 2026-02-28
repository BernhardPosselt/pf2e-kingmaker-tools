package at.posselt.pfrpg2e.app

import at.posselt.pfrpg2e.app.forms.parseFormData
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.newInstance
import at.posselt.pfrpg2e.utils.resolveTemplatePath
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.applications.api.ApplicationFormConfiguration
import com.foundryvtt.core.applications.api.ApplicationHeaderControlsEntry
import com.foundryvtt.core.applications.api.ApplicationPosition
import com.foundryvtt.core.applications.api.HandlebarsTemplatePart
import com.foundryvtt.core.applications.api.Window
import com.foundryvtt.core.applications.ux.FormDataExtended
import com.foundryvtt.core.documents.ClientDocument
import com.foundryvtt.core.game
import js.core.Void
import js.objects.Record
import js.objects.recordOf
import kotlinx.coroutines.await
import kotlinx.html.org.w3c.dom.events.Event
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLFormElement
import kotlin.js.Promise

data class MenuControl(
    val label: String,
    val icon: String? = null,
    val action: String,
    val gmOnly: Boolean = false,
)

@JsPlainObject
external interface ValidatedHandlebarsContext : HandlebarsRenderContext {
    val isFormValid: Boolean
}

abstract class FormApp<T : ValidatedHandlebarsContext, O>(
    title: String,
    template: String,
    isDialogForm: Boolean = true,
    submitOnChange: Boolean = true,
    closeOnSubmit: Boolean = false,
    controls: Array<MenuControl> = emptyArray(),
    classes: Set<String> = emptySet(),
    scrollable: Set<String> = emptySet(),
    width: Int? = undefined,
    height: Int? = null,
    id: String? = null,
    resizable: Boolean? = undefined,
    protected val syncedDocument: ClientDocument? = null,
    protected val debug: Boolean = false,
    protected val dataModel: JsClass<out DataModel>,
    protected val filterBlanks: Boolean = true,
//    protected val initial: O
) : App<T>(
    HandlebarsFormApplicationOptions(
        window = Window(
            title = title,
            resizable = resizable,
            controls = controls.map {
                ApplicationHeaderControlsEntry(
                    label = it.label,
                    icon = it.icon,
                    action = it.action,
                    visible = !it.gmOnly || game.user.isGM,
                )
            }.toTypedArray()
        ),
        position = if (height == null) {
            ApplicationPosition(
                width = width,
            )
        } else {
            ApplicationPosition(
                width = width,
                height = height,
            )
        },
        classes = if (isDialogForm) setOf("km-dialog-form").plus(classes).toTypedArray() else classes.toTypedArray(),
        tag = "form",
        form = ApplicationFormConfiguration(
            submitOnChange = submitOnChange,
            closeOnSubmit = closeOnSubmit,
        ),
        parts = recordOf(
            "form" to HandlebarsTemplatePart(
                template = resolveTemplatePath(template),
                scrollable = scrollable.toTypedArray(),
            )
        )
    ).apply {
        id?.let { this.unsafeCast<Record<String, Any>>()["id"] = it }
    }
) {
    init {
        syncedDocument?.apps[super.id] = this
    }

    protected var isFormValid: Boolean = true

    protected fun isValid() =
        if (element is HTMLFormElement) {
            element.reportValidity()
        } else {
            throw IllegalStateException("Application ${this::class.simpleName} does not posses an outermost form element")
        }

    override fun onSubmit(event: Event, form: HTMLFormElement, formData: FormDataExtended<AnyObject>): Promise<Void> =
        buildPromise {
            val value = formData.`object`
            isFormValid = form.reportValidity()
            if (debug) {
                console.log("Received ${JSON.stringify(value)}")
                console.log("Form is ${if (isFormValid) "valid" else "invalid"}")
            }
            val parsedData = parseFormData<O>(value, filterBlanks = filterBlanks, ::fixObject)
            if (debug) {
                console.log("Parsed object ${JSON.stringify(parsedData)}")
            }
            val model = try {
                dataModel.newInstance(arrayOf(parsedData))
            } catch (e: Throwable) {
                if (debug) {
                    console.log(e)
                }
                isFormValid = false
                null
            }
            val dataModelData = model
                ?.toObject()
                ?.unsafeCast<O>()
                ?: parsedData
            if (debug) {
                console.log("Data model object ${JSON.stringify(dataModelData)}")
            }
            onParsedSubmit(dataModelData).await()
            if (syncedDocument == null) {
                render()
            }
            null
        }

    protected open fun fixObject(value: dynamic) {

    }

    protected abstract fun onParsedSubmit(value: O): Promise<Void>
}
