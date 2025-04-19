package com.foundryvtt.core.helpers

import js.objects.ReadonlyRecord
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface FormInputConfig<T> {
    val name: String
    val value: T
    val required: Boolean?
    val disabled: Boolean?
    val readonly: Boolean?
    val localize: Boolean?
    val dataset: ReadonlyRecord<String, String>?
    val placeholder: String?
    val input: CustomFormInput<T>?
}