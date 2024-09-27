package com.foundryvtt.core.layers

import com.foundryvtt.core.AnyObject
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface LayerOptions {
    val name: String
    val baseClass: JsClass<out CanvasLayer>
}

open external class CanvasLayer {
    // : PIXI.Container
    @OptIn(ExperimentalStdlibApi::class)
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
