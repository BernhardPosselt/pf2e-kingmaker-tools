package com.foundryvtt.core.documents.abstract

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface SearchOptions {
    val query: String?
    val filters: Array<FieldFilter>?
    val exclude: Array<String>
}