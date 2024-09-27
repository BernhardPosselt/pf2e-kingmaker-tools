package com.foundryvtt.core.applications.api

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ContextMenuEntry {
    val name: String
    val icon: String
    val classes: Array<String>
    val group: String
    val callback: (Any) -> Unit // jQuery
    val condition: Any // Boolean or ContextMenuCondition
}