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
    val disabled: Boolean = false,
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
        select = false,
        time = true,
        required = required,
        number = false,
        disabled = disabled,
        text = false,
        radio = false,
        image = false,
        textArea = false,
        checkbox = false,
        options = emptyArray(),
        hideLabel = hideLabel,
        stacked = stacked,
        elementClasses = elementClasses.joinToString(" "),
        menu = false,
        hidden = false,
        escapeLabel = escapeLabel,
        component = false,
        labelElement = labelElement,
    )
}