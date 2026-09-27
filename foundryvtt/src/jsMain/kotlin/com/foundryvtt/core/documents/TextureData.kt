package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface TextureData {
    var src: String
    var tint: Int
    var anchorX: Int
    var anchorY: Int
    var alphaThreshold: Double
    var fit: String
    var offsetX: Int?
    var offsetY: Int?
    var scaleX: Double
    var scaleY: Double
}