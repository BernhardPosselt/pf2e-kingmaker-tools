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
        required = false,
        disabled = false,
        type = "component",
        options = emptyArray(),
        hideLabel = hideLabel,
        stacked = stacked,
        elementClasses = "",
        labelClasses = "",
        escapeLabel = escapeLabel,
        templatePartial = templatePartial,
        labelElement = labelElement,
        readonly = false,
    )
}