@file:JsQualifier("foundry.canvas")

package com.foundryvtt.core.canvas

import com.foundryvtt.core.canvas.layers.DrawingsLayer
import com.foundryvtt.core.canvas.layers.TilesLayer
import com.foundryvtt.core.canvas.layers.TokenLayer
import js.collections.JsSet


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

    // I give up, someone else can continue
    val tokens: TokenLayer
    val drawings: DrawingsLayer
    val tiles: TilesLayer
}