package at.posselt.pfrpg2e.app.forms

data class TextArea(
    override val label: String,
    override val name: String,
    val value: String,
    val required: Boolean = true,
    override val help: String? = null,
    override val hideLabel: Boolean = false,
    val overrideType: OverrideType? = null,
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
        type ="textarea",
        required = required,
        disabled = disabled,
        options = emptyArray(),
        overrideType = overrideType?.value,
        hideLabel = hideLabel,
        stacked = stacked,
        elementClasses = elementClasses.joinToString(" "),
        escapeLabel = escapeLabel,
        labelElement = labelElement,
        readonly = false,
    )
}
