package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface TrackedAttributesDescription {
    val bar: Array<String>
    var value: Array<String>
}