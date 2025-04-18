package com.foundryvtt.core.applications.ux.TextEditor

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface TruncateOptions {
    val maxLength: Int?
    val splitWords: Boolean?
    val suffix: String?
}