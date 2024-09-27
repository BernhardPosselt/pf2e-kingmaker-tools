package com.foundryvtt.core.layers

import com.foundryvtt.core.documents.TileDocument

external class TilesLayer : PlaceablesLayer<TileDocument, Tile> {
    val tiles: Array<Tile>
}