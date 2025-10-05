@file:JsQualifier("PIXI")
package com.pixijs

external class Polygon(val points: Array<Point>) {
    fun contains(x: Int, y: Int): Boolean
}