package com.foundryvtt.core.documents

import com.foundryvtt.core.canvas.layers.PlaceablesLayer
import com.foundryvtt.core.canvas.placeables.PlaceableObject

open external class CanvasDocument: ClientDocument {
    val `object`: PlaceableObject<CanvasDocument>?
    val layer: PlaceablesLayer<CanvasDocument, PlaceableObject<CanvasDocument>>
    val rendered: Boolean
}