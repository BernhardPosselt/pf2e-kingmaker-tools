package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ExportCompendiumOptions {
    val updateByName: Boolean?
    val keepId: Boolean?
    val keepFolder: Boolean?
    val folder: String?
}