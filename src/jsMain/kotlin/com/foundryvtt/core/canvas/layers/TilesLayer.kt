@file:JsQualifier("foundry.canvas.layers")
package com.foundryvtt.core.canvas.layers

import com.foundryvtt.core.canvas.placeables.Tile
import com.foundryvtt.core.documents.TileDocument

external class TilesLayer : PlaceablesLayer<TileDocument, Tile> {
    val tiles: Array<Tile>
}