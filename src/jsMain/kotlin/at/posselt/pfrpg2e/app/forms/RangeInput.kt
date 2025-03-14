package at.posselt.pfrpg2e.app.forms


data class RangeInput(
    override val label: String,
    override val name: String,
    val value: Int = 0,
    val required: Boolean = true,
    override val help: String? = null,
    override val hideLabel: Boolean = false,
    val elementClasses: List<String> = emptyList(),
    val readonly: Boolean = false,
    val stacked: Boolean = true,
    val escapeLabel: Boolean = true,
    val labelElement: String = "label",
    val disabled: Boolean = false,
    val min: Int = 0,
    val max: Int,
    val step: Int = 1,
) : IntoFormElementContext {
    override fun toContext() = FormElementContext(
        isFormElement = true,
        label = label,
        name = name,
        help = help,
        value = value,
        type = "range",
        required = required,
        disabled = disabled,
        options = emptyArray(),
        hideLabel = hideLabel,
        stacked = stacked,
        elementClasses = elementClasses.joinToString(" "),
        escapeLabel = escapeLabel,
        labelElement = labelElement,
        readonly = readonly,
        min=min,
        max=max,
        step=step,
    )
}



