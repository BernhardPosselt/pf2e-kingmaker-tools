package com.foundryvtt.core.canvas

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface SceneTextures {
    val background: String
    val foreground: String
    val fogOverlay: String
}