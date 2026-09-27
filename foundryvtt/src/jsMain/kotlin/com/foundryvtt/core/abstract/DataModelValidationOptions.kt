package com.foundryvtt.core.abstract

import com.foundryvtt.core.AnyObject
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface DataModelValidationOptions {
    val changes: AnyObject?
    val clean: Boolean?
    val fallback: Boolean?
    val dropInvalidEmbedded: Boolean?
    val strict: Boolean?
    val fields: Boolean?
    val joint: Boolean?
}