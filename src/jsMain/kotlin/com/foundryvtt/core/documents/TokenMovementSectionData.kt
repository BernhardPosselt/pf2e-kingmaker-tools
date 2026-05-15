package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface TokenMovementSectionData {
    val waypoints: Array<TokenMeasuredMovementWaypoint>
    val distance: Int
    val cost: Int
    val spaces: Int
    val diagonals: Int
}