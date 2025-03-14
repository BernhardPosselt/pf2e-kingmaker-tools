package at.posselt.pfrpg2e.app.forms

import at.posselt.pfrpg2e.toDataAttributeKey
import at.posselt.pfrpg2e.utils.toRecord

data class Menu(
    override val label: String,
    override val name: String = "",
    override val help: String? = null,
    override val hideLabel: Boolean = false,
    val value: String,
    val stacked: Boolean = false,
    val escapeLabel: Boolean = true,
    val labelElement: String = "span",
    val disabled: Boolean = false,
    val data: List<DataAttribute> = emptyList(),
) : IntoFormElementContext {
    override fun toContext() = FormElementContext(
        isFormElement = true,
        label = label,
        name = name,
        help = help,
        value = value,
        type ="menu",
        required = false,
        disabled = disabled,
        options = emptyArray(),
        hideLabel = hideLabel,
        stacked = stacked,
        elementClasses = "",
        labelClasses = "",
        escapeLabel = escapeLabel,
        labelElement = labelElement,
        data = data.map { it.key.toDataAttributeKey() to it.value }.toRecord(),
        readonly = false,
    )
}