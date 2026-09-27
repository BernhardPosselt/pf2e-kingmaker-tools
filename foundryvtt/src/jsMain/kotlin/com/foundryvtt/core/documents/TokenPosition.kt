package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface TokenPosition {
    val x: Int
    val y: Int
    val elevation: Int
    val width: Int
    val height: Int
    val shape: String
}