package com.foundryvtt.core.data.fields

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface DataModelValidationFailureOptions {
    val invalidValue: Any
    val message: String
    val unresolved: Boolean
}

external class DataModelValidationFailure(options: DataModelValidationFailureOptions)