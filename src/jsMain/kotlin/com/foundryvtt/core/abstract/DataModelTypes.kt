package com.foundryvtt.core.abstract

import kotlinx.js.JsPlainObject


@JsPlainObject
external interface DataValidationOptions {
    val strict: Boolean?
    val fallback: Boolean?
    val partial: Boolean?
    val dropInvalidEmbedded: Boolean?
}

@JsPlainObject
external interface DocumentConstructionContext {
    val parent: Document
    val strict: Boolean?
    val options: DataValidationOptions?
}