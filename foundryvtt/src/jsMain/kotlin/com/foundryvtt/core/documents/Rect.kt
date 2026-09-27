package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface Rect {
    val x: Int
    val y: Int
    val width: Int
    val height: Int
}