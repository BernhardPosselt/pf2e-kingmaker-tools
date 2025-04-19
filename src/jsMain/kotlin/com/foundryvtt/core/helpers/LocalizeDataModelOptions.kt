package com.foundryvtt.core.helpers

import js.objects.JsPlainObject

@JsPlainObject
external interface LocalizeDataModelOptions {
    val prefixes: Array<String>?
    val prefixPath: String?
}