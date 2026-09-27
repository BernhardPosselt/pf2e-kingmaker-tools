package com.foundryvtt.core.documents

import com.foundryvtt.core.abstract.DataModel
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface TokenMeasuredMovementWaypoint {
    val x: Int
    val y: Int
    val elevation: Int
    val width: Int
    val height: Int
    val shape: String
    val action: String
    val terrain: DataModel?
    val snapped: Boolean
    val explicit: Boolean
    val checkpoint: Boolean
    val intermediate: Boolean
    val userId: String
    val movementId: String
    val cost: Int
}