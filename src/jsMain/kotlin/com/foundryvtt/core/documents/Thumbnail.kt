package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface Thumbnail {
    val thumb: String
    val width: Int
    val height: Int
    // TODO: src, texture
}