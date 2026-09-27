@file:JsQualifier("foundry.utils")
package com.foundryvtt.core.utils

import js.array.Tuple3

external class Color(
    var value: Int
) {
    val valid: Boolean
    val css: String
    val rgb: Tuple3<Int, Int, Int>
    val r: Int
    val g: Int
    val b: Int
    val maximum: Int
    val minimum: Int
}