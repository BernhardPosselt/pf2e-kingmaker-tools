package com.foundryvtt.core.applications.apps.FilePicker

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface BrowseOptions {
    val bucket: String?
    val extensions: Array<String>?
    val wildcard: Boolean?
}