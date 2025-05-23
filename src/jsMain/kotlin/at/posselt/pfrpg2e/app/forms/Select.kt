package at.posselt.pfrpg2e.app.forms

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.data.actor.Attribute
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.lowercaseFirst
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.documents.Actor
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
    val labelClasses: List<String> = emptyList(),
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
        required = required,
        type = "select",
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
        labelClasses = labelClasses.joinToString(" "),
        link = if (actor != null) {
            DocumentLinkContext(uuid = actor.uuid, img = actor.img)
        } else if (item != null) {
            DocumentLinkContext(uuid = item.uuid, img = item.img)
        } else {
            null
        },
        escapeLabel = escapeLabel,
        labelElement = labelElement,
        readonly = false,
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
            label: String? = null,
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
            label = label ?: t("applications.dc"),
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
            label: String? = null,
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
            label = label ?: t("applications.level"),
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

        inline fun <reified T> fromEnum(
            label: String? = null,
            name: String,
            value: T? = null,
            required: Boolean = true,
            help: String? = null,
            hideLabel: Boolean = false,
            labelFunction: (T) -> String = { t(it) },
            disabled: Boolean = false,
            stacked: Boolean = true,
            elementClasses: List<String> = emptyList(),
            labelClasses: List<String> = emptyList(),
            escapeLabel: Boolean = true,
        ) where T : Translatable, T : Enum<T>, T : ValueEnum = Select(
            name = name,
            label = label ?: t("enums.${T::class.simpleName?.lowercaseFirst()}"),
            value = value?.value,
            required = required,
            help = help,
            hideLabel = hideLabel,
            options = enumToOptions<T>(labelFunction),
            disabled = disabled,
            stacked = stacked,
            elementClasses = elementClasses,
            labelClasses = labelClasses,
            escapeLabel = escapeLabel,
        )

        fun range(
            label: String,
            name: String,
            from: Int,
            to: Int,
            value: Int,
            required: Boolean = true,
            help: String? = null,
            hideLabel: Boolean = false,
            disabled: Boolean = false,
            stacked: Boolean = true,
            elementClasses: List<String> = emptyList(),
            labelClasses: List<String> = emptyList(),
            escapeLabel: Boolean = true,
        ) = Select(
            name = name,
            label = label,
            value = value.toString(),
            required = required,
            help = help,
            hideLabel = hideLabel,
            options = (from..to)
                .map { SelectOption(it.toString(), it.toString()) }
                .toList(),
            disabled = disabled,
            stacked = stacked,
            elementClasses = elementClasses,
            escapeLabel = escapeLabel,
            overrideType = OverrideType.NUMBER,
            labelClasses = labelClasses,
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
        this@toOption.id?.let {
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

inline fun <reified T> enumToOptions(labelFunction: (T) -> String) where T : Translatable, T : Enum<T>, T : ValueEnum =
    enumEntries<T>().map {
        SelectOption(
            label = labelFunction(it),
            value = it.value
        )
    }

fun Attribute.toOption() =
    SelectOption(
        label = t(this),
        value = value,
    )