package at.posselt.pfrpg2e.kingdom.scenes

import com.foundryvtt.core.documents.DrawingDocument
import com.foundryvtt.core.documents.TileDocument
import com.foundryvtt.core.documents.TokenDocument


fun TileDocument.toRectangle() =
    Rectangle(x = x, y = y, width = width.toDouble(), height = height.toDouble())

fun TokenDocument.toRectangle(squareWidth: Double, squareHeight: Double) =
    Rectangle(x = x, y = y, width = width.toDouble() * squareWidth, height = height.toDouble() * squareHeight)

fun DrawingDocument.toRectangle() =
    Rectangle(x = x, y = y, width = shape.width.toDouble(), height = shape.height.toDouble())