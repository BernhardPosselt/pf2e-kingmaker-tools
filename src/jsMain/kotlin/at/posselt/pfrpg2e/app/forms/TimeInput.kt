package at.posselt.pfrpg2e.app.forms

import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

data class TimeInput(
    override val label: String,
    override val name: String,
    val value: LocalTime,
    val required: Boolean = true,
    override val help: String? = null,
    override val hideLabel: Boolean = false,
    val elementClasses: List<String> = emptyList(),
    val readonly: Boolean = false,
    val stacked: Boolean = true,
    val escapeLabel: Boolean = true,
    val labelElement: String = "label",
) : IntoFormElementContext {
    override fun toContext() = FormElementContext(
        isFormElement = true,
        label = label,
        name = name,
        help = help,
        value = value.format(LocalTime.Format {
            hour(padding = Padding.ZERO)
            char(':')
            minute(padding = Padding.ZERO)
        }),
        type ="time",
        required = required,
        disabled = false,
        options = emptyArray(),
        hideLabel = hideLabel,
        stacked = stacked,
        elementClasses = elementClasses.joinToString(" "),
        escapeLabel = escapeLabel,
        labelElement = labelElement,
        readonly = readonly,
    )
}