package com.foundryvtt.core.applications.api

import io.kvision.jquery.JQuery
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ContextMenuEntry {
    val name: String
    val icon: String
    val classes: Array<String>?
    val group: String?
    val callback: (JQuery) -> Unit
    val condition: (JQuery) -> Unit // Boolean or ContextMenuCondition
}