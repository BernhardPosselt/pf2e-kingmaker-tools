package com.foundryvtt.core.canvas.layers

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface LayerOptions {
    val name: String
    val baseClass: JsClass<out CanvasLayer>
}