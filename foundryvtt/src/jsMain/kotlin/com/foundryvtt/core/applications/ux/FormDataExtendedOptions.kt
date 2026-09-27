package com.foundryvtt.core.applications.ux

import js.objects.ReadonlyRecord
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface FormDataExtendedOptions {
    val dtypes: ReadonlyRecord<String, String>?
    val editors: ReadonlyRecord<String, Any>?
    val disabled: Boolean?
    val readonly: Boolean?
}