package at.posselt.pfrpg2e.kingdom.map

// ── Explored drawing helpers (pure logic, testable without Foundry runtime) ──

const val EXPLORED_DRAWING_TYPE = "explored"

// Visual constants for the explored hex drawing — distinct from claimed (solid fill).
// Explored uses a dashed outline + fog-of-war tint so GMs can tell them apart at a glance.
const val EXPLORED_FILL_COLOR = "#1a3a5c"
const val EXPLORED_FILL_ALPHA = 0.12
const val EXPLORED_STROKE_COLOR = "#4a90d9"
const val EXPLORED_STROKE_WIDTH = 3
const val EXPLORED_STROKE_DASH = "8,4"   // dash length, gap length — Foundry drawing config format

fun shouldHaveExploredDrawing(explored: Boolean?): Boolean = explored == true

// ── Cleared drawing helpers ──

const val CLEARED_DRAWING_TYPE = "cleared"

// Visual constants for the cleared hex drawing.
// Uses a warm orange fill with a dotted stroke — distinct from claimed (solid green)
// and explored (dashed blue). Orange evokes "worked/cleared" terrain.
const val CLEARED_FILL_COLOR = "#ff8c00"
const val CLEARED_FILL_ALPHA = 0.15
const val CLEARED_STROKE_COLOR = "#ff6600"
const val CLEARED_STROKE_WIDTH = 2
const val CLEARED_STROKE_DASH = "2,4"   // dotted pattern (short dash, wide gap)

fun shouldHaveClearedDrawing(cleared: Boolean?): Boolean = cleared == true
