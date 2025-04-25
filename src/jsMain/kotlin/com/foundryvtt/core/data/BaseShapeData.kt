package com.foundryvtt.core.data

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.Document
import com.foundryvtt.core.abstract.DocumentConstructionContext

abstract external class BaseShapeData(
    data: AnyObject = definedExternally,
    options: DocumentConstructionContext = definedExternally
): Document {
    var type: String
    var hole: Boolean
}