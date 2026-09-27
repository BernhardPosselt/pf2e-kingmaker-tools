package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RingData {
    val enabled: Boolean
    val colors: RingColorData
    val effects: Double
    val subject: RingSubjectData
}