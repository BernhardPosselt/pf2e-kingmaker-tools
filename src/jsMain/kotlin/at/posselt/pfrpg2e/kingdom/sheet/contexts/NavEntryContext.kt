package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.t
import kotlinx.js.JsPlainObject
import kotlin.enums.enumEntries

@JsPlainObject
external interface NavEntryContext {
    val label: String
    val active: Boolean
    val link: String
    val title: String
    val action: String
}

inline fun <reified T> createTabs(
    action: String,
    active: T? = null,
) where T: Enum<T>, T: Translatable = enumEntries<T>().map {
    NavEntryContext(
        label = t(it),
        active = it == active,
        link = it.toCamelCase(),
        action = action,
        title = t(it),
    )
}.toTypedArray()
