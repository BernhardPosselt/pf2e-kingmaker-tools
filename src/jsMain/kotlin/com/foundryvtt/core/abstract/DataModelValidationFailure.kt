package com.foundryvtt.core.abstract

import com.foundryvtt.core.data.fields.DataModelValidationFailure
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface DataModelValidationFailures {
    val fields: DataModelValidationFailure
    val joint: DataModelValidationFailure
}