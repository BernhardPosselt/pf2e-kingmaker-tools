package at.posselt.pfrpg2e.camping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KingmakerZoneNamesTest {

    @Test
    fun mapsZoneNumberToCanonicalZoneName() {
        assertEquals("Brevoy", kingmakerZoneNamesByRegionName["Zone 00"])
        assertEquals("Greenbelt", kingmakerZoneNamesByRegionName["Zone 02"])
        assertEquals("Branthlend Mountains", kingmakerZoneNamesByRegionName["Zone 19"])
    }

    @Test
    fun coversAllTwentyKingmakerZones() {
        assertEquals(20, kingmakerZoneNamesByRegionName.size)
    }

    @Test
    fun returnsNullForCustomRegionNames() {
        assertNull(kingmakerZoneNamesByRegionName["My Custom Region"])
    }

    // ── dropdown label (the exact text rendered as each <option>) ─────────

    @Test
    fun dropdownLabelEnrichesKingmakerZonesWithTheirName() {
        assertEquals("Zone 00 - Brevoy", regionDropdownLabel("Zone 00"))
        assertEquals("Zone 05 - Narlmarches", regionDropdownLabel("Zone 05"))
        assertEquals("Zone 19 - Branthlend Mountains", regionDropdownLabel("Zone 19"))
    }

    @Test
    fun dropdownLabelTrimsStoredRegionNameWhenMatching() {
        assertEquals("Zone 00  - Brevoy", regionDropdownLabel("Zone 00 "))
    }

    @Test
    fun dropdownLabelLeavesCustomRegionsUnchanged() {
        assertEquals("My Custom Region", regionDropdownLabel("My Custom Region"))
    }
}
