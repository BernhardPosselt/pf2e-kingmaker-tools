package com.foundryvtt.core.data

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DocumentConstructionContext

external class CircleShapeData(
    data: AnyObject = definedExternally,
    options: DocumentConstructionContext = definedExternally
): BaseShapeData {
    var radius: Int
    var x: Int
    var y: Int
}