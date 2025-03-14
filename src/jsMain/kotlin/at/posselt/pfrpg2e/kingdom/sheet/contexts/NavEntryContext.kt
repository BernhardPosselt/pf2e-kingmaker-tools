package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel
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

inline fun <reified T : Enum<T>> createTabs(
    action: String,
    active: T? = null,
) = enumEntries<T>().map {
    NavEntryContext(
        label = it.toLabel(),
        active = it == active,
        link = it.toCamelCase(),
        action = action,
        title = it.toLabel(),
    )
}.toTypedArray()
