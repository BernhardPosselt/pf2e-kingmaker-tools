@file:JsQualifier("foundry.canvas.layers")
package com.foundryvtt.core.canvas.layers

open external class InteractionLayer : CanvasLayer {
    val active: Boolean

    fun activate(options: ActivateOptions): InteractionLayer
    fun deactivate(): InteractionLayer
    fun getZIndex(): Int
}