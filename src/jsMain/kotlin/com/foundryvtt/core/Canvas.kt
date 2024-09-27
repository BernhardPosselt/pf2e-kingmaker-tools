package com.foundryvtt.core

import com.foundryvtt.core.layers.DrawingsLayer
import com.foundryvtt.core.layers.TilesLayer
import com.foundryvtt.core.layers.TokenLayer
import js.collections.JsSet
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement

typealias CanvasEdges = Any
typealias FogManager = Any
typealias PerceptionManager = Any
typealias PixiFilters = Any
typealias MouseInteractionManager = Any

@JsPlainObject
external interface LoadTextureOptions {
    val expireCache: Boolean
    val additionalSources: Array<String>
}

@JsPlainObject
external interface VisibilityOptions {
    val persistentVision: Boolean
}

@JsPlainObject
external interface SceneTextures {
    val background: String
    val foreground: String
    val fogOverlay: String
}

@JsPlainObject
external interface BlurOptions {
    val enabled: Boolean
    val blurClass: JsClass<*>
    val strength: Int
    val passes: Int
    val kernels: Int
}

@JsPlainObject
external interface FPS {
    val average: Double
    val values: Array<Double>
    val render: Int
    val element: HTMLElement
}


external class Canvas {
    val edges: CanvasEdges
    val fog: FogManager
    val percpetion: PerceptionManager
    var blurFilters: JsSet<PixiFilters>
    var currentMouseManager: MouseInteractionManager
    var mouseInteractionManager: MouseInteractionManager
    var loadTextureOptions: LoadTextureOptions
    var visibilityOptions: VisibilityOptions
    var sceneTextures: SceneTextures
    val fps: FPS

    // I give up, someone else continue
    val tokens: TokenLayer
    val drawings: DrawingsLayer
    val tiles: TilesLayer
}