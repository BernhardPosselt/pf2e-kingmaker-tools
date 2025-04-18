package com.foundryvtt.core.canvas

import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement

@JsPlainObject
external interface FPS {
    val average: Double
    val values: Array<Double>
    val render: Int
    val element: HTMLElement
}