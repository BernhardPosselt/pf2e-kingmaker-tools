package at.posselt.pfrpg2e.kingdom.scenes

import com.foundryvtt.core.documents.DrawingDocument
import com.foundryvtt.core.documents.TileDocument
import com.foundryvtt.core.documents.TokenDocument

// x and y coordinates in v14 depend on the type of grid
// for square grids, the mark the top left corner
// for hex grids, the x and y coordinates are in the center
enum class GridType {
    HEX,
    SQUARE;
}

fun TileDocument.toRectangle(gridType: GridType) = when (gridType) {
    GridType.SQUARE -> Rectangle(x = x, y = y, width = width.toDouble(), height = height.toDouble())
    GridType.HEX -> Rectangle.fromCenteredCoordinates(
        x = x,
        y = y,
        width = width.toDouble(),
        height = height.toDouble()
    )
}

fun TokenDocument.toRectangle(squareWidth: Double, squareHeight: Double) =
    Rectangle(x = x, y = y, width = width.toDouble() * squareWidth, height = height.toDouble() * squareHeight)

fun DrawingDocument.toRectangle(gridType: GridType) = when (gridType) {
    GridType.SQUARE -> Rectangle(x = x, y = y, width = shape.width.toDouble(), height = shape.height.toDouble())
    GridType.HEX -> Rectangle.fromCenteredCoordinates(
        x = x,
        y = y,
        width = shape.width.toDouble(),
        height = shape.height.toDouble()
    )
}