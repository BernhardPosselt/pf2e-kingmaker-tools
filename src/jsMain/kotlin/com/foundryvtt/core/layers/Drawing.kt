package com.foundryvtt.core.layers

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.documents.DrawingDocument

external class Drawing : PlaceableObject<DrawingDocument> {
    companion object {
        fun rescaleDimensions(original: AnyObject, x: Double, y: Double): AnyObject
        fun normalizeShape(data: AnyObject): AnyObject
    }

    val isAuthor: Boolean
    val isVisible: Boolean
    val isTiled: Boolean
    val isPolygon: Boolean
    val hasText: Boolean
    val type: String

    fun enableTextEditing(options: AnyObject = definedExternally)
}