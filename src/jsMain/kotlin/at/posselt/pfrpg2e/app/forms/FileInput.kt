package at.posselt.pfrpg2e.app.forms

data class FileInput(
    override val label: String,
    override val name: String,
    val accept: List<String> = emptyList(),
    val required: Boolean = true,
    override val help: String? = null,
    override val hideLabel: Boolean = false,
    val elementClasses: List<String> = emptyList(),
    val labelClasses: List<String> = emptyList(),
    val stacked: Boolean = true,
    val escapeLabel: Boolean = true,
    val labelElement: String = "label",
) : IntoFormElementContext {
    override fun toContext() = FormElementContext(
        isFormElement = true,
        label = label,
        name = name,
        help = help,
        required = required,
        type = "file",
        disabled = false,
        options = emptyArray(),
        hideLabel = hideLabel,
        stacked = stacked,
        elementClasses = elementClasses.joinToString(" "),
        labelClasses = labelClasses.joinToString(" "),
        escapeLabel = escapeLabel,
        labelElement = labelElement,
        accept = accept.joinToString(","),
        readonly = false,
    )
}

