package at.posselt.pfrpg2e.app.forms

import at.posselt.pfrpg2e.data.actor.Attribute
import at.posselt.pfrpg2e.data.actor.Perception
import at.posselt.pfrpg2e.deCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel
import com.foundryvtt.core.Actor
import com.foundryvtt.core.documents.Item
import com.foundryvtt.core.documents.JournalEntry
import com.foundryvtt.core.documents.JournalEntryPage
import com.foundryvtt.core.documents.Playlist
import com.foundryvtt.core.documents.PlaylistSound
import com.foundryvtt.core.documents.RollTable
import kotlin.enums.enumEntries

data class Select(
    override val label: String,
    override val name: String,
    val value: String? = null,
    val options: List<SelectOption>,
    val required: Boolean = true,
    val overrideType: OverrideType? = null,
    override val help: String? = null,
    override val hideLabel: Boolean = false,
    val elementClasses: List<String> = emptyList(),
    val disabled: Boolean = false,
    val stacked: Boolean = true,
    val actor: Actor? = null,
    val item: Item? = null,
    val escapeLabel: Boolean = true,
    val labelElement: String = "label",
) : IntoFormElementContext {
    override fun toContext() = FormElementContext(
        isFormElement = true,
        label = label,
        name = name,
        help = help,
        value = value,
        select = true,
        required = required,
        number = false,
        text = false,
        time = false,
        textArea = false,
        checkbox = false,
        radio = false,
        image = false,
        disabled = disabled,
        stacked = stacked,
        overrideType = overrideType?.value,
        options = options.map { opt ->
            Option(
                label = opt.label,
                value = opt.value,
                selected = opt.value == value,
                classes = opt.classes.joinToString(" "),
            )
        }.toTypedArray(),
        hideLabel = hideLabel,
        elementClasses = elementClasses.joinToString(" "),
        menu = false,
        hidden = false,
        link = if (actor != null) {
            DocumentLinkContext(uuid = actor.uuid, img = actor.img)
        } else if (item != null) {
            DocumentLinkContext(uuid = item.uuid, img = item.img)
        } else {
            null
        },
        escapeLabel = escapeLabel,
        component = false,
        labelElement = labelElement,
    )

    companion object {
        fun flatCheck(
            label: String,
            name: String,
            value: Int? = null,
            required: Boolean = true,
            help: String? = null,
            hideLabel: Boolean = false,
            disabled: Boolean = false,
            stacked: Boolean = true,
            elementClasses: List<String> = emptyList(),
            escapeLabel: Boolean = true,
        ) = Select(
            label = label,
            name = name,
            value = value.toString(),
            required = required,
            help = help,
            elementClasses = elementClasses,
            hideLabel = hideLabel,
            overrideType = OverrideType.NUMBER,
            options = generateSequence(1) { it + 1 }
                .take(20)
                .map { SelectOption(it.toString(), it.toString()) }
                .toList(),
            disabled = disabled,
            stacked = stacked,
            escapeLabel = escapeLabel,
        )

        fun dc(
            label: String = "DC",
            name: String = "dc",
            value: Int? = null,
            required: Boolean = true,
            help: String? = null,
            hideLabel: Boolean = false,
            disabled: Boolean = false,
            stacked: Boolean = true,
            elementClasses: List<String> = emptyList(),
            escapeLabel: Boolean = true,
        ) = Select(
            label = label,
            name = name,
            value = value.toString(),
            required = required,
            help = help,
            hideLabel = hideLabel,
            overrideType = OverrideType.NUMBER,
            options = generateSequence(0) { it + 1 }
                .take(61)
                .map { SelectOption(it.toString(), it.toString()) }
                .toList(),
            disabled = disabled,
            stacked = stacked,
            elementClasses = elementClasses,
            escapeLabel = escapeLabel,
        )

        fun level(
            label: String = "Level",
            name: String = "level",
            value: Int? = null,
            required: Boolean = true,
            help: String? = null,
            hideLabel: Boolean = false,
            disabled: Boolean = false,
            stacked: Boolean = true,
            elementClasses: List<String> = emptyList(),
            escapeLabel: Boolean = true,
        ) = Select(
            label = label,
            name = name,
            value = value.toString(),
            required = required,
            help = help,
            hideLabel = hideLabel,
            overrideType = OverrideType.NUMBER,
            options = generateSequence(-1) { it + 1 }
                .take(27)
                .map { SelectOption(it.toString(), it.toString()) }
                .toList(),
            disabled = disabled,
            stacked = stacked,
            elementClasses = elementClasses,
            escapeLabel = escapeLabel,
        )

        inline fun <reified T : Enum<T>> fromEnum(
            label: String,
            name: String,
            value: T? = null,
            required: Boolean = true,
            help: String? = null,
            hideLabel: Boolean = false,
            labelFunction: (T) -> String = { it.toLabel() },
            disabled: Boolean = false,
            stacked: Boolean = true,
            elementClasses: List<String> = emptyList(),
            escapeLabel: Boolean = true,
        ) = Select(
            name = name,
            label = label,
            value = value?.toCamelCase(),
            required = required,
            help = help,
            hideLabel = hideLabel,
            options = enumToOptions<T>(labelFunction),
            disabled = disabled,
            stacked = stacked,
            elementClasses = elementClasses,
            escapeLabel = escapeLabel,
        )
    }
}

data class SelectOption(
    val label: String,
    val value: String,
    val classes: List<String> = emptyList(),
)


fun RollTable.toOption(useUuid: Boolean = false) =
    if (useUuid) {
        SelectOption(label = name, value = uuid)
    } else {
        id?.let {
            SelectOption(label = name, value = it)
        }
    }

fun Playlist.toOption(useUuid: Boolean = false) =
    if (useUuid) {
        SelectOption(label = name, value = uuid)
    } else {
        id?.let {
            SelectOption(label = name, value = it)
        }
    }

fun PlaylistSound.toOption(useUuid: Boolean = false) =
    if (useUuid) {
        SelectOption(label = name, value = uuid)
    } else {
        id?.let {
            SelectOption(label = name, value = it)
        }
    }

fun Actor.toOption(useUuid: Boolean = false) =
    if (useUuid) {
        SelectOption(label = name, value = uuid)
    } else {
        id?.let {
            SelectOption(label = name, value = it)
        }
    }

fun JournalEntry.toOption(useUuid: Boolean = false) =
    if (useUuid) {
        SelectOption(label = name, value = uuid)
    } else {
        id?.let {
            SelectOption(label = name, value = it)
        }
    }

fun JournalEntryPage.toOption(useUuid: Boolean = false) =
    if (useUuid) {
        SelectOption(label = name, value = uuid)
    } else {
        id?.let {
            SelectOption(label = name, value = it)
        }
    }


fun Item.toOption(useUuid: Boolean = false) =
    if (useUuid) {
        name?.let {
            SelectOption(label = it, value = uuid)
        }
    } else {
        id?.let { id ->
            name?.let { name ->
                SelectOption(label = name, value = id)
            }
        }
    }

inline fun <reified T : Enum<T>> enumToOptions(labelFunction: (T) -> String = { it.toLabel() }) =
    enumEntries<T>().map {
        SelectOption(
            label = labelFunction(it),
            value = it.toCamelCase()
        )
    }

fun Attribute.toOption() =
    SelectOption(
        label = value.deCamelCase(),
        value = value,
    )