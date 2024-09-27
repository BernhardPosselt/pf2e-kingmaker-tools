package com.foundryvtt.core.data.fields

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface StringFieldOptions : DataFieldOptions/*<String>*/ {
    var blank: Boolean?
    var trim: Boolean?
    var choices: Any? // Array<String> | Object | function
    var textSearch: Boolean?
}