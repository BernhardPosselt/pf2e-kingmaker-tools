package at.posselt.pfrpg2e.data.kingdom.settlements

import at.posselt.pfrpg2e.data.kingdom.structures.AvailableItemBonuses
import at.posselt.pfrpg2e.data.kingdom.structures.CommodityStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private fun createMinimalSettlement(
    magicalStreetlamps: Boolean = false,
    pavedStreets: Boolean = false,
    sewerSystem: Boolean = false,
    lotsBorderingWater: Int = 0,
    edges: SettlementEdges = SettlementEdges(),
    urbanGrid: UrbanGrid = UrbanGrid(),
) = Settlement(
    id = "test",
    name = "Test Settlement",
    type = SettlementType.SETTLEMENT,
    waterBorders = 0,
    isSecondaryTerritory = false,
    settlementEventBonus = 0,
    leaderLeadershipActivityBonus = 0,
    bonuses = emptySet(),
    allowCapitalInvestment = false,
    notes = emptySet(),
    storage = CommodityStorage(ore = 0, food = 0, lumber = 0, stone = 0, luxuries = 0),
    increaseLeadershipActivities = false,
    consumptionReduction = 0,
    availableItems = AvailableItemBonuses(),
    size = SettlementSize(
        type = SettlementSizeType.VILLAGE,
        maximumBlocks = "1",
        requiredKingdomLevel = 1,
        population = "<401",
        consumption = 1,
        maxItemBonus = 1,
        influence = 0,
        levelFrom = 1,
        levelTo = 1,
    ),
    unlockActivities = emptySet(),
    residentialLots = 4,
    hasBridge = false,
    occupiedBlocks = 0,
    preventItemLevelPenalty = false,
    delayedStructures = emptyList(),
    constructedStructures = emptyList(),
    structuresUnderConstruction = emptyList(),
    maximumCivicRdLimit = 0,
    settlementActions = 0,
    blocks = emptyList(),
    layoutType = SettlementLayoutType.RIGID,
    magicalStreetlamps = magicalStreetlamps,
    pavedStreets = pavedStreets,
    sewerSystem = sewerSystem,
    lotsBorderingWater = lotsBorderingWater,
    edges = edges,
    urbanGrid = urbanGrid,
)

class SettlementUrbanFieldsTest {

    @Test
    fun defaultSettlementHasMagicalStreetlampsFalse() {
        val settlement = createMinimalSettlement()
        assertFalse(settlement.magicalStreetlamps)
    }

    @Test
    fun defaultSettlementHasPavedStreetsFalse() {
        val settlement = createMinimalSettlement()
        assertFalse(settlement.pavedStreets)
    }

    @Test
    fun defaultSettlementHasSewerSystemFalse() {
        val settlement = createMinimalSettlement()
        assertFalse(settlement.sewerSystem)
    }

    @Test
    fun defaultSettlementHasZeroLotsBorderingWater() {
        val settlement = createMinimalSettlement()
        assertEquals(0, settlement.lotsBorderingWater)
    }

    @Test
    fun defaultSettlementHasEmptyEdges() {
        val settlement = createMinimalSettlement()
        val edges = settlement.edges
        assertFalse(edges.north.hasWater)
        assertFalse(edges.east.hasWater)
        assertFalse(edges.south.hasWater)
        assertFalse(edges.west.hasWater)
    }

    @Test
    fun defaultSettlementHasDefaultUrbanGrid() {
        val settlement = createMinimalSettlement()
        val grid = settlement.urbanGrid
        assertEquals(9, grid.blocks.size)
        assertEquals(0, grid.totalWaterLots)
        assertEquals(0, grid.occupiedBlocks)
    }

    @Test
    fun canSetMagicalStreetlampsTrue() {
        val settlement = createMinimalSettlement(magicalStreetlamps = true)
        assertTrue(settlement.magicalStreetlamps)
    }

    @Test
    fun canSetPavedStreetsTrue() {
        val settlement = createMinimalSettlement(pavedStreets = true)
        assertTrue(settlement.pavedStreets)
    }

    @Test
    fun canSetSewerSystemTrue() {
        val settlement = createMinimalSettlement(sewerSystem = true)
        assertTrue(settlement.sewerSystem)
    }

    @Test
    fun canSetLotsBorderingWater() {
        val settlement = createMinimalSettlement(lotsBorderingWater = 3)
        assertEquals(3, settlement.lotsBorderingWater)
    }

    @Test
    fun canSetCustomEdges() {
        val customEdges = SettlementEdges(
            north = UrbanGridEdge(hasWater = true, hasBridge = true),
            east = UrbanGridEdge(hasWoodWall = true),
            south = UrbanGridEdge(hasStoneWall = true),
            west = UrbanGridEdge(),
        )
        val settlement = createMinimalSettlement(edges = customEdges)
        assertTrue(settlement.edges.north.hasWater)
        assertTrue(settlement.edges.north.hasBridge)
        assertTrue(settlement.edges.east.hasWoodWall)
        assertTrue(settlement.edges.south.hasStoneWall)
        assertFalse(settlement.edges.west.hasWater)
    }

    @Test
    fun canSetCustomUrbanGrid() {
        val customGrid = UrbanGrid(
            blockA = BlockGrid(topLeft = BlockTerrain.WATER, topRight = BlockTerrain.WATER),
            blockB = BlockGrid(topLeft = BlockTerrain.PAVED),
        )
        val settlement = createMinimalSettlement(urbanGrid = customGrid)
        assertEquals(2, settlement.urbanGrid.totalWaterLots)
        assertEquals(2, settlement.urbanGrid.occupiedBlocks)
    }

    @Test
    fun canSetAllNewFieldsTogether() {
        val edges = SettlementEdges(north = UrbanGridEdge(hasWater = true))
        val grid = UrbanGrid(blockA = BlockGrid(topLeft = BlockTerrain.WATER))
        val settlement = createMinimalSettlement(
            magicalStreetlamps = true,
            pavedStreets = true,
            sewerSystem = true,
            lotsBorderingWater = 1,
            edges = edges,
            urbanGrid = grid,
        )
        assertTrue(settlement.magicalStreetlamps)
        assertTrue(settlement.pavedStreets)
        assertTrue(settlement.sewerSystem)
        assertEquals(1, settlement.lotsBorderingWater)
        assertTrue(settlement.edges.north.hasWater)
        assertEquals(1, settlement.urbanGrid.totalWaterLots)
    }

    @Test
    fun copyPreservesNewFields() {
        val settlement = createMinimalSettlement(
            magicalStreetlamps = true,
            pavedStreets = true,
            sewerSystem = true,
            lotsBorderingWater = 2,
        )
        val copied = settlement.copy()
        assertTrue(copied.magicalStreetlamps)
        assertTrue(copied.pavedStreets)
        assertTrue(copied.sewerSystem)
        assertEquals(2, copied.lotsBorderingWater)
    }

    @Test
    fun copyAllowsOverridingNewFields() {
        val settlement = createMinimalSettlement(magicalStreetlamps = true)
        val modified = settlement.copy(magicalStreetlamps = false)
        assertFalse(modified.magicalStreetlamps)
        assertTrue(settlement.magicalStreetlamps)
    }
}
