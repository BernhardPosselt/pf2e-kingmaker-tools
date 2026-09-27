package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface PanToNoteOptions {
    val scale: Double?
    val duration: Int?
}