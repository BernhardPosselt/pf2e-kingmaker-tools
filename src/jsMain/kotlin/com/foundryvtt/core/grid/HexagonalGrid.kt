@file:JsQualifier("foundry.grid")

package com.foundryvtt.core.grid

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface HexagonalGridConfiguration : GridConfiguration {
    val columns: Boolean
    val even: Boolean
    val diagonals: Int
}

open external class HexagonalGrid(
    override val config: HexagonalGridConfiguration
) : GridlessGrid {
    val columns: Boolean
    val even: Boolean
    val diagonals: String
}