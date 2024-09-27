package at.posselt.pfrpg2e.app

import at.posselt.pfrpg2e.app.forms.parseFormData
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.newInstance
import at.posselt.pfrpg2e.utils.resolveTemplatePath
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.FormDataExtended
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.applications.api.ApplicationFormConfiguration
import com.foundryvtt.core.applications.api.ApplicationHeaderControlsEntry
import com.foundryvtt.core.applications.api.ApplicationPosition
import com.foundryvtt.core.applications.api.HandlebarsTemplatePart
import com.foundryvtt.core.applications.api.Window
import com.foundryvtt.core.game
import js.core.Void
import js.objects.Record
import js.objects.recordOf
import kotlinx.coroutines.await
import kotlinx.html.org.w3c.dom.events.Event
import org.w3c.dom.HTMLFormElement
import kotlin.js.Promise

data class MenuControl(
    val label: String,
    val icon: String? = null,
    val action: String,
    val gmOnly: Boolean = false,
)

abstract class FormApp<T : HandlebarsRenderContext, O>(
    title: String,
    template: String,
    isDialogForm: Boolean = true,
    submitOnChange: Boolean = true,
    closeOnSubmit: Boolean = false,
    controls: Array<MenuControl> = emptyArray(),
    classes: Array<String> = emptyArray(),
    scrollable: Array<String> = emptyArray(),
    width: Int? = undefined,
    height: Int? = null,
    id: String? = null,
    resizable: Boolean? = undefined,
    protected val debug: Boolean = false,
    protected val renderOnSubmit: Boolean = true,
    protected val dataModel: JsClass<out DataModel>? = null,
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
        classes = if (isDialogForm) arrayOf("km-dialog-form").plus(classes) else classes,
        tag = "form",
        form = ApplicationFormConfiguration(
            submitOnChange = submitOnChange,
            closeOnSubmit = closeOnSubmit,
        ),
        parts = recordOf(
            "form" to HandlebarsTemplatePart(
                template = resolveTemplatePath(template),
                scrollable = scrollable,
            )
        )
    ).apply {
        id?.let { this.unsafeCast<Record<String, Any>>()["id"] = it }
    }
) {
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
            val parsedData = parseFormData<O>(value, ::fixObject)
            if (debug) {
                console.log("Parsed object ${JSON.stringify(parsedData)}")
            }
            val dataModelData = dataModel
                ?.newInstance(arrayOf(parsedData))
                ?.toObject()
                ?.unsafeCast<O>()
                ?: parsedData
            if (debug) {
                console.log("Datamodel object ${JSON.stringify(dataModelData)}")
            }
            onParsedSubmit(dataModelData).await()
            if (renderOnSubmit) {
                render()
            }
            null
        }

    protected open fun fixObject(value: dynamic) {

    }

    protected abstract fun onParsedSubmit(value: O): Promise<Void>
}
