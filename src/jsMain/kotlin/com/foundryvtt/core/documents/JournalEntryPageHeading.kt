package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLHeadingElement

@JsPlainObject
external interface JournalEntryPageHeading {
    val number: Int
    val text: String
    val slug: String
    val element: HTMLHeadingElement
    val children: Array<String>
    val order: Int
}