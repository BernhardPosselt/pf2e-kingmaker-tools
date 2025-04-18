package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface SceneDimensions {
    val width: Int
    val height: Int
    val size: Int
    val sceneX: Int
    val sceneY: Int
    val sceneWidth: Int
    val sceneHeight: Int
    val rect: Rect
    val sceneRect: Rect
    val distance: Int
    val distancePixels: Int
    val ration: Double
    val maxR: Int
    val rows: Int
    val columns: Int
}