@file:JsQualifier("foundry.grid")
package com.foundryvtt.core.grid

import com.foundryvtt.core.utils.Color
import com.pixijs.Point
import js.array.Tuple4
import js.objects.JsPlainObject

@JsPlainObject
external interface CalculatedDimensions {
    val width: Int
    val height: Int
    val x: Int
    val y: Int
    val rows: Int
    val columns: Int
}

@JsPlainObject
external interface Rectangle {
    val width: Int
    val height: Int
    val x: Int
    val y: Int
}


typealias Coordinates2D = Any // Point|GridOffset2D

open external class BaseGrid(
    open val config: GridConfiguration
) {
    val size: Int
    val sizeX: Int
    val sizeY: Int
    val distance: Int
    val units: String
    val style: String
    val thickness: Int
    val color: Color
    val alpha: Double
    val type: Int
    val isGridless: Boolean
    val isSquare: Boolean
    val isHexagonal: Boolean
    fun calculateDimensions(sceneWidth: Int, sceneHeight: Int, padding: Int): CalculatedDimensions
    fun getOffset(coords: Coordinates2D): GridOffset2D
    fun getOffsetRange(offsetRange: Rectangle): Tuple4<Int, Int, Int, Int>
    fun getAdjacentOffsets(coords: Coordinates2D): Array<GridOffset2D>
    fun testAdjacency(coords1: Coordinates2D, coords2: Coordinates2D): Boolean
    fun getShiftedOffset(coords1: Coordinates2D, direction: Int)
    fun getShiftedPoint(point: Point, direction: Int)
    fun getTopLeftPoint(coords: Coordinates2D): Point
    fun getCenterPoint(coords: Coordinates2D): Point
    fun getShape(): Array<Point>
    fun getVertices(coords: Coordinates2D): Array<Point>
    // fun getSnappedPoint(point: Point, behavior: GridSnappingBehavior): Point
    // fun measurePath(): GridMeasurePathResult
    // fun getDirectPath
    // fun getTranslatedPoint
    // fun getCircle
    // fun getCone
}