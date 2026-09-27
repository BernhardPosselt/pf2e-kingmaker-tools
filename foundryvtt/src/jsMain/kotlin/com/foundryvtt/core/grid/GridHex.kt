@file:JsQualifier("foundry.grid")
package com.foundryvtt.core.grid

import com.pixijs.Point
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface GridOffset2D {
    val i: Int
    val j: Int
}

@JsPlainObject
external interface HexagonalGridCube2D {
    val q: Int
    val r: Int
    val s: Int
}

open external class GridHex(
    coordinates: Point,
    grid: HexagonalGrid,
) {
    constructor(
        coordinates: HexagonalGridCube2D,
        grid: HexagonalGrid,
    )

    constructor(
        coordinates: GridOffset2D,
        grid: HexagonalGrid,
    )

    val coordinates: Any
    val grid: HexagonalGrid
    var cube: HexagonalGridCube2D
    var offset: GridOffset2D
    val center: Point
    val topLeft: Point

    fun getNeighbors(): Array<GridHex>
    fun shiftCube(dq: Int, dr: Int, ds: Int)
}