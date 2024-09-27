package at.posselt.pfrpg2e.app.forms

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface Option {
    val label: String
    val value: String
    val selected: Boolean
    val classes: String
}

@JsPlainObject
external interface SectionsContext {
    val sections: Array<SectionContext>
}

@JsPlainObject
external interface SectionContext {
    val hidden: Boolean?
    val legend: String
    val formRows: Array<FormElementContext>
}

@JsPlainObject
external interface DocumentLinkContext {
    val uuid: String
    val img: String?
}

@JsPlainObject
external interface FormElementContext {
    val label: String
    val name: String
    val help: String?
    val value: Any?
    val required: Boolean
    val options: Array<Option>
    val hideLabel: Boolean
    val overrideType: String?
    val isFormElement: Boolean
    val elementClasses: String
    val disabled: Boolean
    val stacked: Boolean
    val link: DocumentLinkContext?
    val escapeLabel: Boolean
    val templatePartial: String?
    val labelElement: String
    val placeholder: String?
    val checked: Boolean?

    // instead of going with an enum, we need to list all values
    // because Handlebars is fucking stupid and can't have == in their template language
    val select: Boolean
    val number: Boolean
    val time: Boolean
    val text: Boolean
    val component: Boolean
    val textArea: Boolean
    val checkbox: Boolean
    val menu: Boolean
    val hidden: Boolean
    val radio: Boolean
    val image: Boolean
}


sealed interface IntoFormElementContext {
    val label: String
    val name: String
    val help: String?
    val hideLabel: Boolean

    fun toContext(): FormElementContext
}


data class Section(
    val legend: String,
    val formRows: List<IntoFormElementContext>
) {
    fun toContext(): SectionContext = SectionContext(
        legend = legend,
        formRows = formRows.map(IntoFormElementContext::toContext).toTypedArray()
    )
}

/**
 * Custom function to build a form declaratively rather than having
 * one template for each form
 */
fun formContext(vararg rows: IntoFormElementContext): Array<FormElementContext> =
    rows.map { it.toContext() }.toTypedArray()


fun formContext(vararg rows: Section): Array<SectionContext> =
    rows.map { it.toContext() }.toTypedArray()
