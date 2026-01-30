package com.foundryvtt.core.helpers

import js.intl.ListFormatStyle
import js.intl.ListFormatType
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface GetListFormatterOptions {
    val style: ListFormatStyle?
    val type: ListFormatType?
}