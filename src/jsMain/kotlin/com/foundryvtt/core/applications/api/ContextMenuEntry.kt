package com.foundryvtt.core.applications.api

import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement

@JsPlainObject
external interface ContextMenuEntry {
    val name: String
    val icon: String
    val classes: Array<String>?
    val group: String?
    val callback: (HTMLElement) -> Unit
    val condition: (HTMLElement) -> Boolean // Boolean or ContextMenuCondition
}