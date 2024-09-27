package com.foundryvtt.core.layers

import com.foundryvtt.core.documents.TileDocument
import org.w3c.dom.HTMLElement

external class Tile : PlaceableObject<TileDocument> {
    val aspectRation: Double
    val sourceElement: HTMLElement
    val isVideo: Boolean
    val isVisible: Boolean
    val occluded: Boolean
    val playing: Boolean
    val volume: Double
}