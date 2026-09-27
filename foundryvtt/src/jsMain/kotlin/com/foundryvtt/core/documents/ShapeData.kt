package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ShapeData {
    val type: String
    val width: Int
    val height: Int
}