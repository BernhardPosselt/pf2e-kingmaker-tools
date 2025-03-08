package at.posselt.pfrpg2e.kingdom.sheet.contexts

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface NavEntryContext {
    val label: String
    val active: Boolean
    val link: String
    val title: String
}