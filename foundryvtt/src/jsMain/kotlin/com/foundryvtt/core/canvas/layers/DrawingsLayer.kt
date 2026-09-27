package com.foundryvtt.core.canvas.layers

import com.foundryvtt.core.canvas.placeables.Drawing
import com.foundryvtt.core.documents.DrawingDocument

external class DrawingsLayer : PlaceablesLayer<DrawingDocument, Drawing> {
    val drawings: Array<Drawing>
}