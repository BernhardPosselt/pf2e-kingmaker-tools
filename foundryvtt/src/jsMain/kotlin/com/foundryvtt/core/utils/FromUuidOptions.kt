package com.foundryvtt.core.utils

import com.foundryvtt.core.abstract.Document
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface FromUuidOptions {
    val relative: Document?
    val invalid: Boolean?
}