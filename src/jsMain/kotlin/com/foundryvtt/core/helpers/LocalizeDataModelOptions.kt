package com.foundryvtt.core.helpers

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface LocalizeDataModelOptions {
    val prefixes: Array<String>?
    val prefixPath: String?
}