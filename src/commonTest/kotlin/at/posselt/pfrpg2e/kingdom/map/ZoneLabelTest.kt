package at.posselt.pfrpg2e.kingdom.map

import at.posselt.pfrpg2e.data.regions.Terrain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneLabelTest {

    // ── Zone grouping logic ──────────────────────────────────────────────
    // Mirrors the grouping done in syncZoneLabels: hexes grouped by zone.id

    private data class FakeZone(
        val id: String,
        val label: String,
        val color: String,
        val level: Int,
        val terrain: String,
        val travel: String,
    )

    private data class FakeHex(
        val name: String,
        val zone: FakeZone,
        val offsetI: Int,
        val offsetJ: Int,
    )

    private fun groupHexesByZone(hexes: List<FakeZoneEntry>): Map<String, List<FakeZoneEntry>> =
        hexes.groupBy { it.zoneId }

    private data class FakeZoneEntry(
        val zoneId: String,
        val zoneLabel: String,
        val zoneColor: String,
        val centerX: Double,
        val centerY: Double,
    )

    private fun computeZoneLabels(hexes: List<FakeHex>): List<FakeZoneEntry> {
        return hexes
            .groupBy { it.zone.id }
            .map { (zoneId, hexList) ->
                val zoneLabel = hexList[0].zone.label
                val zoneColor = hexList[0].zone.color
                val centerX = hexList.map { it.offsetI * 100.0 }.average()
                val centerY = hexList.map { it.offsetJ * 100.0 }.average()
                FakeZoneEntry(
                    zoneId = zoneId,
                    zoneLabel = zoneLabel,
                    zoneColor = zoneColor,
                    centerX = centerX,
                    centerY = centerY,
                )
            }
    }

    @Test
    fun `groupHexesByZone groups multiple hexes with same zone`() {
        val zones = listOf(
            FakeZoneEntry("BV", "Brevoy", "#FF0000", 100.0, 200.0),
            FakeZoneEntry("BV", "Brevoy", "#FF0000", 200.0, 300.0),
            FakeZoneEntry("GB", "Greenbelt", "#00FF00", 500.0, 600.0),
        )
        val grouped = groupHexesByZone(zones)
        assertEquals(2, grouped.size)
        assertEquals(2, grouped["BV"]?.size)
        assertEquals(1, grouped["GB"]?.size)
    }

    @Test
    fun `computeZoneLabels creates one label per zone`() {
        val hexes = listOf(
            FakeHex("Hex 1", FakeZone("BV", "Brevoy", "#FF0000", 0, "plains", "normal"), 0, 0),
            FakeHex("Hex 2", FakeZone("BV", "Brevoy", "#FF0000", 0, "plains", "normal"), 1, 0),
            FakeHex("Hex 3", FakeZone("GB", "Greenbelt", "#00FF00", 2, "forest", "normal"), 5, 5),
        )
        val labels = computeZoneLabels(hexes)
        assertEquals(2, labels.size)
        val brevoys = labels.filter { it.zoneId == "BV" }
        assertEquals(1, brevoys.size)
        assertEquals("Brevoy", brevoys[0].zoneLabel)
        assertEquals("#FF0000", brevoys[0].zoneColor)
    }

    @Test
    fun `computeZoneLabels computes center point correctly`() {
        val hexes = listOf(
            FakeHex("Hex 1", FakeZone("BV", "Brevoy", "#FF0000", 0, "plains", "normal"), 0, 0),
            FakeHex("Hex 2", FakeZone("BV", "Brevoy", "#FF0000", 0, "plains", "normal"), 2, 0),
        )
        val labels = computeZoneLabels(hexes)
        assertEquals(1, labels.size)
        // Center of (0,0) and (200,0) = (100, 0)
        assertEquals(100.0, labels[0].centerX)
        assertEquals(0.0, labels[0].centerY)
    }

    @Test
    fun `computeZoneLabels handles single hex zone`() {
        val hexes = listOf(
            FakeHex("Hex 1", FakeZone("TV", "Thousand Voices", "#0000FF", 18, "forest", "normal"), 10, 10),
        )
        val labels = computeZoneLabels(hexes)
        assertEquals(1, labels.size)
        assertEquals("Thousand Voices", labels[0].zoneLabel)
        assertEquals("TV", labels[0].zoneId)
        assertEquals(1000.0, labels[0].centerX)
        assertEquals(1000.0, labels[0].centerY)
    }

    @Test
    fun `computeZoneLabels handles all 20 kingmaker zones`() {
        val zoneDefs = listOf(
            "BV" to "Brevoy",
            "RL" to "Rostland Hinterlands",
            "GB" to "Greenbelt",
            "TW" to "Tuskwater",
            "KL" to "Kamelands",
            "NM" to "Narlmarches",
            "SH" to "Sellen Hills",
            "DS" to "Dunsward",
            "NH" to "Nomen Heights",
            "LV" to "Tor of Levenies",
            "HT" to "Hooktongue",
            "DR" to "Drelev",
            "TL" to "Tiger Lords",
            "RU" to "Rushlight",
            "GL" to "Glenebon Lowlands",
            "PX" to "Pitax",
            "GU" to "Glenebon Uplands",
            "NU" to "Numeria",
            "TV" to "Thousand Voices",
            "BR" to "Branthlend Mountains",
        )
        val hexes = zoneDefs.mapIndexed { index, (id, label) ->
            FakeHex(
                name = "Hex $id",
                zone = FakeZone(id, label, "#${index.toString(16).padStart(6, '0')}", index, "forest", "normal"),
                offsetI = index,
                offsetJ = index,
            )
        }
        val labels = computeZoneLabels(hexes)
        assertEquals(20, labels.size)
        // Each zone should have exactly one label
        val labelIds = labels.map { it.zoneId }.toSet()
        assertEquals(20, labelIds.size)
        // Verify all zone names are present
        val labelTexts = labels.map { it.zoneLabel }.toSet()
        zoneDefs.forEach { (_, name) ->
            assertTrue(labelTexts.contains(name), "Missing zone label: $name")
        }
    }

    @Test
    fun `computeZoneLabels uses zone color from first hex`() {
        val hexes = listOf(
            FakeHex("Hex 1", FakeZone("BV", "Brevoy", "#AABBCC", 0, "plains", "normal"), 0, 0),
            FakeHex("Hex 2", FakeZone("BV", "Brevoy", "#DDEEFF", 0, "plains", "normal"), 1, 0),
        )
        val labels = computeZoneLabels(hexes)
        assertEquals(1, labels.size)
        // Should use color from first hex
        assertEquals("#AABBCC", labels[0].zoneColor)
    }

    @Test
    fun `computeZoneLabels center is average of all hex positions`() {
        val hexes = listOf(
            FakeHex("H1", FakeZone("Z1", "Zone One", "#111111", 0, "plains", "normal"), 0, 0),
            FakeHex("H2", FakeZone("Z1", "Zone One", "#111111", 0, "plains", "normal"), 3, 0),
            FakeHex("H3", FakeZone("Z1", "Zone One", "#111111", 0, "plains", "normal"), 0, 3),
            FakeHex("H4", FakeZone("Z1", "Zone One", "#111111", 0, "plains", "normal"), 3, 3),
        )
        val labels = computeZoneLabels(hexes)
        assertEquals(1, labels.size)
        // Center of (0,0), (300,0), (0,300), (300,300) = (150, 150)
        assertEquals(150.0, labels[0].centerX)
        assertEquals(150.0, labels[0].centerY)
    }
}
