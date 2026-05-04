package at.posselt.pfrpg2e.kingdom.scenes

data class Rectangle(val x: Double, val y: Double, val width: Double, val height: Double) {
    companion object {
        fun fromCenteredCoordinates(x: Double, y: Double, width: Double, height: Double) =
            Rectangle(x - width / 2, y - height / 2, width, height)
    }

    val xEnd = x + width
    val yEnd = y + height

    fun applyTolerance(toleranceInPx: Double = 50.0) =
        copy(
            x = x - toleranceInPx,
            y = y - toleranceInPx,
            height = height + 2 * toleranceInPx,
            width = width + 2 * toleranceInPx
        )

    operator fun contains(other: Rectangle) =
        other.x >= x
                && other.y >= y
                && other.xEnd <= xEnd
                && other.yEnd <= yEnd
}