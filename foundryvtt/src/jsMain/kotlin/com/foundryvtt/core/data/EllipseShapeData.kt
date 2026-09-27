@file:JsQualifier("foundry.data")
package com.foundryvtt.core.data

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DocumentConstructionContext

external class EllipseShapeData(
    data: AnyObject = definedExternally,
    options: DocumentConstructionContext = definedExternally
): BaseShapeData {
    var radiusX: Int
    var radiusY: Int
    var rotation: Int
    var x: Int
    var y: Int
}