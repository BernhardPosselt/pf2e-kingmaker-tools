package at.posselt.pfrpg2e.app.forms


data class NumberInput(
    override val label: String,
    override val name: String,
    val value: Int = 0,
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
        value = value,
        select = false,
        time = false,
        required = required,
        disabled = disabled,
        number = true,
        text = false,
        image = false,
        textArea = false,
        checkbox = false,
        options = emptyArray(),
        hideLabel = hideLabel,
        stacked = stacked,
        elementClasses = elementClasses.joinToString(" "),
        menu = false,
        hidden = false,
        radio = false,
        escapeLabel = escapeLabel,
        component = false,
        labelElement = labelElement,
    )
}



