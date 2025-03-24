package com.foundryvtt.core.abstract

import com.foundryvtt.core.data.fields.DataModelValidationFailure
import js.objects.JsPlainObject

@JsPlainObject
external interface DataModelValidationFailures {
    val fields: DataModelValidationFailure
    val joint: DataModelValidationFailure
}