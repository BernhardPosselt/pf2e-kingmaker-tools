package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface PolygonData: ShapeData {
    val points: Array<Double>
}