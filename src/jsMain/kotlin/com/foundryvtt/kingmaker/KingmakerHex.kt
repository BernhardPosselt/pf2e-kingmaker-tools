package com.foundryvtt.kingmaker

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.grid.GridHex
import com.foundryvtt.core.grid.GridOffset2D
import com.foundryvtt.core.grid.HexagonalGrid
import com.foundryvtt.core.grid.HexagonalGridCube2D
import com.foundryvtt.core.utils.Color
import com.pixijs.Point
import js.objects.JsPlainObject

@JsPlainObject
external interface HexOffsetCoordinate {
    val i: Int
    val j: Int
}

@JsPlainObject
external interface Terrain {
    val id: String
    val img: String
    val label: String
}

@JsPlainObject
external interface Zone {
    val id: String
    val color: String
    val label: String
    val level: Int
    val terrain: String
    val travel: String
    val polygon: Array<Int>
}


external class KingmakerHex(
    coordinates: Point,
    grid: HexagonalGrid,
): GridHex {
    constructor(
        coordinates: HexagonalGridCube2D,
        grid: HexagonalGrid,
    )

    constructor(
        coordinates: GridOffset2D,
        grid: HexagonalGrid,
    )

    val key: Int
    val name: String
    val zone: Zone
    val terrain: Terrain
    val travel: AnyObject // TODO
    val difficulty: AnyObject // TODO
    val discoveryTrait: AnyObject // TODO
    val explorationState: AnyObject // TODO
    val color: Color

    companion object {
        @JsStatic
        fun getKey(offset: HexOffsetCoordinate): Int
    }
}