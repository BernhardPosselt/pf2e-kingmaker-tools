package at.posselt.pfrpg2e.app.forms

import at.posselt.pfrpg2e.toDataAttributeKey
import at.posselt.pfrpg2e.utils.toRecord


data class Button(
    override val label: String,
    override val name: String = "",
    override val help: String? = null,
    override val hideLabel: Boolean = true,
    val value: String,
    val stacked: Boolean = false,
    val escapeLabel: Boolean = true,
    val labelElement: String = "span",
    val disabled: Boolean = false,
    val icon: String? = null,
    val data: List<DataAttribute> = emptyList(),
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
        disabled = disabled,
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
        component = false,
        labelElement = labelElement,
        data = data.map { it.key.toDataAttributeKey() to it.value }.toRecord(),
        button = true,
        empty = false,
        icon = icon,
    )
}