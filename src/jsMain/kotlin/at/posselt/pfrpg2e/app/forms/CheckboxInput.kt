package at.posselt.pfrpg2e.app.forms

data class CheckboxInput(
    override val label: String,
    override val name: String,
    val value: Boolean = false,
    val required: Boolean = false,
    override val help: String? = null,
    override val hideLabel: Boolean = false,
    val elementClasses: List<String> = emptyList(),
    val disabled: Boolean = false,
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
        select = false,
        time = false,
        required = required,
        number = false,
        text = false,
        textArea = false,
        checkbox = true,
        image = false,
        radio = false,
        disabled = disabled,
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

