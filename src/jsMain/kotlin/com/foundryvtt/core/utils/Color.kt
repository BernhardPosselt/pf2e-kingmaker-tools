@file:JsQualifier("foundry.utils")
package com.foundryvtt.core.utils

import js.array.JsTuple3

external class Color(
    var value: Int
) {
    val valid: Boolean
    val css: String
    val rgb: JsTuple3<Int, Int, Int>
    val r: Int
    val g: Int
    val b: Int
    val maximum: Int
    val minimum: Int
}