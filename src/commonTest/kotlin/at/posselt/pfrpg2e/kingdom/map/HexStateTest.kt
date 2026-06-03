package at.posselt.pfrpg2e.kingdom.map

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Pure-logic tests for HexState explored field transitions.
 *
 * Mirrors the HexState external interface from KingmakerModule.kt
 * (commodity, camp, features, claimed, explored) using a plain Kotlin
 * data class so tests run in commonTest without JS interop.
 */
class HexStateTest {

    // ── Model mirror ──────────────────────────────────────────────────────

    private data class HexFeature(val type: String?)

    private data class HexState(
        val commodity: String? = null,
        val camp: String? = null,
        val features: Array<HexFeature>? = null,
        val claimed: Boolean? = null,
        val explored: Boolean? = null,
    )

    private fun createHex(
        commodity: String? = null,
        camp: String? = null,
        features: Array<HexFeature>? = null,
        claimed: Boolean? = null,
        explored: Boolean? = null,
    ) = HexState(
        commodity = commodity,
        camp = camp,
        features = features,
        claimed = claimed,
        explored = explored,
    )

    // ── Create ────────────────────────────────────────────────────────────

    @Test
    fun `create hex with explored true`() {
        val hex = createHex(explored = true)
        assertTrue(hex.explored == true)
    }

    @Test
    fun `create hex with explored false`() {
        val hex = createHex(explored = false)
        assertFalse(hex.explored == true)
    }

    @Test
    fun `create hex without explored field defaults to null`() {
        val hex = createHex()
        assertNull(hex.explored)
    }

    @Test
    fun `create hex with all fields including explored`() {
        val hex = createHex(
            commodity = "lumber",
            camp = "lumber",
            features = arrayOf(HexFeature("forest")),
            claimed = true,
            explored = true,
        )
        assertEquals("lumber", hex.commodity)
        assertEquals("lumber", hex.camp)
        assertEquals(1, hex.features?.size)
        assertTrue(hex.claimed == true)
        assertTrue(hex.explored == true)
    }

    // ── Update ────────────────────────────────────────────────────────────

    @Test
    fun `update explored from null to true`() {
        val hex = createHex()
        assertNull(hex.explored)
        val updated = hex.copy(explored = true)
        assertTrue(updated.explored == true)
    }

    @Test
    fun `update explored from true to false`() {
        val hex = createHex(explored = true)
        assertTrue(hex.explored == true)
        val updated = hex.copy(explored = false)
        assertFalse(updated.explored == true)
    }

    @Test
    fun `update explored from false to null`() {
        val hex = createHex(explored = false)
        val updated = hex.copy(explored = null)
        assertNull(updated.explored)
    }

    // ── Delete (nullify) ──────────────────────────────────────────────────

    @Test
    fun `delete explored by setting to null`() {
        val hex = createHex(explored = true)
        assertTrue(hex.explored == true)
        val deleted = hex.copy(explored = null)
        assertNull(deleted.explored)
    }

    // ── Mixed state transitions ───────────────────────────────────────────

    @Test
    fun `explored true and claimed false`() {
        val hex = createHex(claimed = false, explored = true)
        assertFalse(hex.claimed == true)
        assertTrue(hex.explored == true)
    }

    @Test
    fun `explored false and claimed true`() {
        val hex = createHex(claimed = true, explored = false)
        assertTrue(hex.claimed == true)
        assertFalse(hex.explored == true)
    }

    @Test
    fun `both explored and claimed true`() {
        val hex = createHex(claimed = true, explored = true)
        assertTrue(hex.claimed == true)
        assertTrue(hex.explored == true)
    }

    @Test
    fun `hex state transition lifecycle - unexplored to explored to claimed`() {
        // Phase 1: new hex, nothing set
        val initial = createHex()
        assertNull(initial.explored)
        assertNull(initial.claimed)

        // Phase 2: mark as explored
        val afterExplore = initial.copy(explored = true)
        assertTrue(afterExplore.explored == true)
        assertNull(afterExplore.claimed)

        // Phase 3: mark as claimed (explored stays true)
        val afterClaim = afterExplore.copy(claimed = true)
        assertTrue(afterClaim.explored == true)
        assertTrue(afterClaim.claimed == true)

        // Phase 4: un-explore (revert explored, keep claimed)
        val afterUnexplore = afterClaim.copy(explored = false)
        assertFalse(afterUnexplore.explored == true)
        assertTrue(afterUnexplore.claimed == true)
    }

    @Test
    fun `explored field does not interfere with other fields`() {
        val hex = createHex(
            commodity = "ore",
            camp = "mine",
            features = arrayOf(HexFeature("mine")),
            claimed = true,
            explored = true,
        )
        assertEquals("ore", hex.commodity)
        assertEquals("mine", hex.camp)
        assertEquals(1, hex.features?.size)
        assertEquals("mine", hex.features?.get(0)?.type)
        assertTrue(hex.claimed == true)
        assertTrue(hex.explored == true)
    }

    // ── Explored drawing decision tests (pure logic, no Foundry runtime) ──

    @Test
    fun `shouldHaveExploredDrawing returns true when explored is true`() {
        assertTrue(shouldHaveExploredDrawing(true))
    }

    @Test
    fun `shouldHaveExploredDrawing returns false when explored is false`() {
        assertFalse(shouldHaveExploredDrawing(false))
    }

    @Test
    fun `shouldHaveExploredDrawing returns false when explored is null`() {
        assertFalse(shouldHaveExploredDrawing(null))
    }

    @Test
    fun `EXPLORED_DRAWING_TYPE constant is explored`() {
        assertEquals("explored", EXPLORED_DRAWING_TYPE)
    }

    @Test
    fun `explored drawing lifecycle - unexplored to explored triggers create`() {
        // Initially not explored
        assertFalse(shouldHaveExploredDrawing(null))
        // After exploring
        assertTrue(shouldHaveExploredDrawing(true))
    }

    @Test
    fun `explored drawing lifecycle - explored to unexplored triggers delete`() {
        // Currently explored
        assertTrue(shouldHaveExploredDrawing(true))
        // After un-exploring
        assertFalse(shouldHaveExploredDrawing(false))
    }

    @Test
    fun `explored drawing lifecycle - explored to null triggers delete`() {
        // Currently explored
        assertTrue(shouldHaveExploredDrawing(true))
        // After removing explored state
        assertFalse(shouldHaveExploredDrawing(null))
    }

    @Test
    fun `explored drawing decision is independent of claimed state`() {
        // Explored true, claimed false → should show explored drawing
        assertTrue(shouldHaveExploredDrawing(true))
        // Explored false, claimed true → should not show explored drawing
        assertFalse(shouldHaveExploredDrawing(false))
        // Both null → should not show explored drawing
        assertFalse(shouldHaveExploredDrawing(null))
    }

    // ── Explored drawing visual constants ──────────────────────────────────

    @Test
    fun `EXPLORED_FILL_COLOR is dark blue-gray fog tint`() {
        assertEquals("#1a3a5c", EXPLORED_FILL_COLOR)
    }

    @Test
    fun `EXPLORED_FILL_ALPHA is subtle`() {
        assertEquals(0.12, EXPLORED_FILL_ALPHA)
    }

    @Test
    fun `EXPLORED_STROKE_COLOR is muted blue`() {
        assertEquals("#4a90d9", EXPLORED_STROKE_COLOR)
    }

    @Test
    fun `EXPLORED_STROKE_WIDTH is thicker than claimed for visibility`() {
        assertEquals(3, EXPLORED_STROKE_WIDTH)
    }

    @Test
    fun `EXPLORED_STROKE_DASH defines dash pattern`() {
        assertEquals("8,4", EXPLORED_STROKE_DASH)
    }

    @Test
    fun `explored visual is distinct from claimed visual`() {
        // Claimed uses solid green fill — explored should differ in every property
        assertFalse(EXPLORED_FILL_COLOR == "#00FF00")
        assertFalse(EXPLORED_STROKE_COLOR == "#00FF00")
        // Explored fill should be more transparent than claimed (0.2)
        assertTrue(EXPLORED_FILL_ALPHA < 0.2)
    }

    // ── Road drawing decision (feature-based) ──────────────────────────────

    @Test
    fun `shouldHaveRoadDrawing is true when a road feature is present`() {
        assertTrue(shouldHaveRoadDrawing(listOf("road")))
    }

    @Test
    fun `shouldHaveRoadDrawing is true when road is among several features`() {
        assertTrue(shouldHaveRoadDrawing(listOf("farmland", "road", "bridge")))
    }

    @Test
    fun `shouldHaveRoadDrawing is false when no road feature is present`() {
        assertFalse(shouldHaveRoadDrawing(listOf("farmland", "ruin")))
    }

    @Test
    fun `shouldHaveRoadDrawing is false for empty or null features`() {
        assertFalse(shouldHaveRoadDrawing(emptyList()))
        assertFalse(shouldHaveRoadDrawing(null))
    }

    @Test
    fun `shouldHaveRoadDrawing ignores null feature types`() {
        assertFalse(shouldHaveRoadDrawing(listOf(null, "farmland")))
        assertTrue(shouldHaveRoadDrawing(listOf(null, "road")))
    }

    @Test
    fun `removing the road feature flips shouldHaveRoadDrawing to false`() {
        val withRoad = listOf("road", "farmland")
        assertTrue(shouldHaveRoadDrawing(withRoad))
        val afterRemoval = withRoad.filterNot { it == "road" }
        assertFalse(shouldHaveRoadDrawing(afterRemoval))
    }

    @Test
    fun `ROAD_FEATURE_TYPE constant matches native lowercase feature type`() {
        assertEquals("road", ROAD_FEATURE_TYPE)
    }
}
