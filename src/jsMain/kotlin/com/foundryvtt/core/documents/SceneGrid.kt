package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface SceneGrid {
    val type: Int
    val size: Int
    val style: String
    val thickness: Int
    val color: String
    val alpha: Double?
    val distance: Int
    val unit: String
}