package com.foundryvtt.core.helpers

import js.objects.ReadonlyRecord
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface SettingsData<T> {
    val name: String?
    val hint: String?
    val scope: String // world or client
    val config: Boolean?
    val default: T?
    val requiresReload: Boolean?
    val type: Any  // Number, Object, Array, Boolean class or data field or data model
    val choices: ReadonlyRecord<String, T>?
    val onChange: ((value: T) -> Unit)?
    val range: SettingsRange?
    val input: CustomFormInput<T>?
}