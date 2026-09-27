package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface SceneInitial {
    val x: Int?
    val y: Int?
    val scale: Double?
}