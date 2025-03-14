package at.posselt.pfrpg2e.app.forms

data class TextInput(
    override val label: String,
    override val name: String,
    val value: String,
    val required: Boolean = true,
    override val help: String? = null,
    override val hideLabel: Boolean = false,
    val overrideType: OverrideType? = null,
    val elementClasses: List<String> = emptyList(),
    val readonly: Boolean = false,
    val stacked: Boolean = true,
    val escapeLabel: Boolean = true,
    val labelElement: String = "label",
    val placeholder: String? = null,
) : IntoFormElementContext {
    override fun toContext() = FormElementContext(
        isFormElement = true,
        label = label,
        name = name,
        help = help,
        value = value,
        required = required,
        type = "text",
        disabled = false,
        readonly = readonly,
        overrideType = overrideType?.value,
        options = emptyArray(),
        hideLabel = hideLabel,
        stacked = stacked,
        elementClasses = elementClasses.joinToString(" "),
        escapeLabel = escapeLabel,
        labelElement = labelElement,
        placeholder = placeholder,
    )
}

