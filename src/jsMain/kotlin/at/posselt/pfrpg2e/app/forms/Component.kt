package at.posselt.pfrpg2e.app.forms

import com.foundryvtt.core.AnyObject

data class Component(
    override val label: String,
    override val name: String = "",
    val value: AnyObject,
    val templatePartial: String,
    override val help: String? = null,
    override val hideLabel: Boolean = false,
    val stacked: Boolean = true,
    val escapeLabel: Boolean = true,
    val labelElement: String = "label",
) : IntoFormElementContext {
    override fun toContext() = FormElementContext(
        isFormElement = true,
        label = label,
        name = name,
        help = help,
        value = value,
        select = false,
        time = false,
        required = false,
        number = false,
        disabled = false,
        text = false,
        image = false,
        textArea = false,
        checkbox = false,
        radio = false,
        options = emptyArray(),
        hideLabel = hideLabel,
        stacked = stacked,
        elementClasses = "",
        menu = false,
        hidden = false,
        escapeLabel = escapeLabel,
        templatePartial = templatePartial,
        component = true,
        labelElement = labelElement,
    )
}