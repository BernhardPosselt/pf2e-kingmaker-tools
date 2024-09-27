package com.foundryvtt.core.layers

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ActivateOptions {
    val tool: String?
}

open external class InteractionLayer : CanvasLayer {
    val active: Boolean

    fun activate(options: ActivateOptions): InteractionLayer
    fun deactivate(): InteractionLayer
    fun getZIndex(): Int
}