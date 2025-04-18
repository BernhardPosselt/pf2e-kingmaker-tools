package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface TileOcclusion {
    val mode: Int
    val alpha: Double
}