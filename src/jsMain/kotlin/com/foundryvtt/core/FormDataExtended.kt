package com.foundryvtt.core

import js.objects.ReadonlyRecord
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLFormElement
import org.w3c.xhr.FormData

@JsPlainObject
external interface FormDataExtendedOptions {
    val dtypes: ReadonlyRecord<String, String>?
    val editors: ReadonlyRecord<String, Any>?
    val disabled: Boolean?
    val readonly: Boolean?
}

external interface ProcessedFormData<T> {
    val value: T
    val writable: Boolean
    val enumerable: Boolean
}

external class FormDataExtended<T>(
    form: HTMLFormElement,
    options: FormDataExtendedOptions? = definedExternally
) : FormData {
    val dtypes: ReadonlyRecord<String, String>?
    val editors: ReadonlyRecord<String, Any>?
    val `object`: T
}