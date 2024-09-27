package com.foundryvtt.core.data.fields

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ArrayFieldOptions<T> : DataFieldOptions/*<Array<T>>*/ {
    val min: Int?
    val max: Int?
}