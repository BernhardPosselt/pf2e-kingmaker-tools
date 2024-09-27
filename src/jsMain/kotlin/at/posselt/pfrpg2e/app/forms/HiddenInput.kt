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
        select = false,
        time = false,
        required = false,
        hidden = true,
        number = false,
        text = false,
        textArea = false,
        checkbox = false,
        image = false,
        radio = false,
        disabled = false,
        options = emptyArray(),
        hideLabel = hideLabel,
        stacked = true,
        elementClasses = "",
        menu = false,
        escapeLabel = escapeLabel,
        overrideType = overrideType?.value,
        component = false,
        labelElement = "label",
    )
}