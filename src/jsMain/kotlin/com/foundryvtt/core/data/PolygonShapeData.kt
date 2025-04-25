@file:JsQualifier("foundry.data")
package com.foundryvtt.core.data

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DocumentConstructionContext

external class PolygonShapeData(
    data: AnyObject = definedExternally,
    options: DocumentConstructionContext = definedExternally
): BaseShapeData {
    var points: Array<Int>
}