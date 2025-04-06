package at.posselt.pfrpg2e.app.forms


data class NumberInput(
    override val label: String,
    override val name: String,
    val value: Int? = 0,
    val required: Boolean = true,
    override val help: String? = null,
    override val hideLabel: Boolean = false,
    val elementClasses: List<String> = emptyList(),
    val labelClasses: List<String> = emptyList(),
    val readonly: Boolean = false,
    val stacked: Boolean = true,
    val escapeLabel: Boolean = true,
    val labelElement: String = "label",
    val disabled: Boolean = false,
) : IntoFormElementContext {
    override fun toContext() = FormElementContext(
        isFormElement = true,
        label = label,
        name = name,
        help = help,
        value = value ?: "",
        type = "number",
        required = required,
        disabled = disabled,
        options = emptyArray(),
        hideLabel = hideLabel,
        stacked = stacked,
        elementClasses = elementClasses.joinToString(" "),
        escapeLabel = escapeLabel,
        labelElement = labelElement,
        readonly = readonly,
        labelClasses = labelClasses.joinToString(" "),
    )
}



