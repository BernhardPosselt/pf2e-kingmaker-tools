package com.foundryvtt.core.applications.apps.FilePicker

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface FavoriteFolder {
    val source: String
    val path: String
    val label: String
}