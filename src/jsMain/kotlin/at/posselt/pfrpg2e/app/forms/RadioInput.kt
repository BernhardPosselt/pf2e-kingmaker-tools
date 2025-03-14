package at.posselt.pfrpg2e.app.forms

data class RadioInput(
    override val label: String,
    override val name: String,
    val checked: Boolean,
    val value: String,
    val required: Boolean = false,
    override val help: String? = null,
    override val hideLabel: Boolean = false,
    val elementClasses: List<String> = emptyList(),
    val labelClasses: List<String> = emptyList(),
    val readonly: Boolean = false,
    val stacked: Boolean = false,
    val escapeLabel: Boolean = true,
    val labelElement: String = "label",
) : IntoFormElementContext {
    override fun toContext() = FormElementContext(
        isFormElement = true,
        label = label,
        name = name,
        help = help,
        value = value,
        type = "radio",
        required = required,
        disabled = false,
        options = emptyArray(),
        hideLabel = hideLabel,
        stacked = stacked,
        elementClasses = elementClasses.joinToString(" "),
        escapeLabel = escapeLabel,
        labelElement = labelElement,
        checked = checked,
        readonly = readonly,
        labelClasses = labelClasses.joinToString(" "),
    )
}

