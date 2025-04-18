@file:JsQualifier("foundry.applications.ux")

package com.foundryvtt.core.applications.ux

import js.objects.ReadonlyRecord
import org.w3c.dom.HTMLFormElement
import org.w3c.xhr.FormData

external class FormDataExtended<T>(
    form: HTMLFormElement,
    options: FormDataExtendedOptions? = definedExternally
) : FormData {
    val dtypes: ReadonlyRecord<String, String>?
    val editors: ReadonlyRecord<String, Any>?
    val `object`: T
}