package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface CursorData {
    val x: Int
    val y: Int
}