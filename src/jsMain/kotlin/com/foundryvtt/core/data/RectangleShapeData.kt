@file:JsQualifier("foundry.data")
package com.foundryvtt.core.data

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DocumentConstructionContext

external class RectangleShapeData(
    data: AnyObject = definedExternally,
    options: DocumentConstructionContext = definedExternally
): BaseShapeData {
    var width: Int
    var height: Int
    var rotation: Int
    var x: Int
    var y: Int
}