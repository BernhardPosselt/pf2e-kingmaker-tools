package at.posselt.pfrpg2e.app.forms

data class ImageUpload(
    override val label: String,
    override val name: String,
    val value: String,
    val required: Boolean = false,
    override val help: String? = null,
    override val hideLabel: Boolean = false,
    val elementClasses: List<String> = emptyList(),
    val disabled: Boolean = false,
    val stacked: Boolean = false,
    val labelElement: String = "label",
) : IntoFormElementContext {
    override fun toContext() = FormElementContext(
        isFormElement = true,
        label = label,
        name = name,
        help = help,
        value = value,
        type ="image",
        required = required,
        disabled = disabled,
        options = emptyArray(),
        hideLabel = hideLabel,
        stacked = stacked,
        elementClasses = elementClasses.joinToString(" "),
        escapeLabel = false,
        labelElement = labelElement,
        readonly = false,
    )
}

