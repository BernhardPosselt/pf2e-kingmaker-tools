package com.foundryvtt.core.layers

import com.foundryvtt.core.documents.DrawingDocument

external class DrawingsLayer : PlaceablesLayer<DrawingDocument, Drawing> {
    val drawings: Array<Drawing>
}