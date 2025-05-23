package at.posselt.pfrpg2e.app.forms

data class HiddenInput(
    override val label: String = "",
    override val name: String,
    val value: String,
    override val help: String? = null,
    override val hideLabel: Boolean = true,
    val overrideType: OverrideType? = null,
    val escapeLabel: Boolean = true,
) : IntoFormElementContext {
    override fun toContext() = FormElementContext(
        isFormElement = true,
        label = label,
        name = name,
        help = help,
        value = value,
        required = false,
        disabled = false,
        type = "hidden",
        options = emptyArray(),
        hideLabel = hideLabel,
        stacked = true,
        elementClasses = "",
        labelClasses = "",
        escapeLabel = escapeLabel,
        overrideType = overrideType?.value,
        labelElement = "label",
        readonly = false,
    )
}