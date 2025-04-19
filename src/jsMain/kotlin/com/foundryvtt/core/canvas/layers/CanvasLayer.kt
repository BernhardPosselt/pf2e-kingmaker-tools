@file:JsQualifier("foundry.canvas.layers")
package com.foundryvtt.core.canvas.layers

import com.foundryvtt.core.AnyObject
import kotlin.js.Promise

open external class CanvasLayer {
    // : PIXI.Container
    @JsExternalInheritorsOnly
    open class CanvasLayerStatic<T> {
        val layerOptions: LayerOptions
        val instance: T
    }

    companion object : CanvasLayerStatic<CanvasLayer>

    var interactiveChildren: Boolean
    val name: String
    val hookName: String

    fun draw(options: AnyObject): Promise<CanvasLayer>
    fun tearDown(options: AnyObject): Promise<CanvasLayer>
}
