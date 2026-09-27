@file:JsQualifier("foundry.grid")
package com.foundryvtt.core.grid

import com.pixijs.ColorSource
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface GridConfiguration {
    val size: Int
    val distance: Int
    val units: String
    val style: String
    val color: ColorSource
    val alpha: Double
    val thickness: Int
}

open external class GridlessGrid(
    override val config: GridConfiguration
): BaseGrid {
}