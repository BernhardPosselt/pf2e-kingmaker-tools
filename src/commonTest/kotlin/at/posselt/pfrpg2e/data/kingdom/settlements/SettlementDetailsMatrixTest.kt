package at.posselt.pfrpg2e.data.kingdom.settlements

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.structures.AvailableItemBonuses
import at.posselt.pfrpg2e.data.kingdom.structures.CommodityStorage
import at.posselt.pfrpg2e.data.kingdom.structures.GroupedStructureBonus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SettlementDetailsMatrixTest {
    @Test
    fun includesWorkbookSettlementsRowsA46ToA145InOrder() {
        assertEquals(
            listOf(
                "Agriculture",
                "Establish Farmland",
                "Harvest Crops",
                "Arts",
                "Craft Luxuries",
                "Create a Masterpiece",
                "Repair Reputation (Corruption)",
                "Rest and Relax (Arts)",
                "Quell Unrest (Arts)",
                "Boating",
                "Go Fishing",
                "Rest and Relax (Boating)",
                "Defense",
                "Fortify Hex",
                "Provide Care",
                "Engineering",
                "Build Roads",
                "Demolish",
                "Establish Work Site",
                "Establish Work Site (Lumber Camp)",
                "Establish Work Site (Mine)",
                "Establish Work Site (Quarry)",
                "Irrigation",
                "Repair Reputation (Decay)",
                "Exploration",
                "Hire Adventurers",
                "Folklore",
                "Celebrate Holiday",
                "Quell Unrest (Folklore)",
                "Industry",
                "Relocate Capital",
                "Repair Reputation (Strife)",
                "Trade Commodities",
                "Intrigue",
                "Clandestine Business",
                "Infiltration",
                "Quell Unrest (Intrigue)",
                "Magic",
                "Prognostication",
                "Quell Unrest (Magic)",
                "Supernatural Solution",
                "Politics",
                "Improve Lifestyle",
                "Quell Unrest (Politics)",
                "Scholarship",
                "Creative Solution",
                "Rest and Relax (Scholarship)",
                "Statecraft",
                "Request Foreign Aid",
                "Send Diplomatic Envoy",
                "Tap Treasury",
                "Trade",
                "Capital Investment",
                "Collect Taxes",
                "Manage Trade Agreements",
                "Purchase Commodities",
                "Repair Reputation (Crime)",
                "Rest and Relax (Trade)",
                "Warfare",
                "Pledge of Fealty (Warfare)",
                "Quell Unrest (Warfare)",
                "Wilderness",
                "Gather Livestock",
                "Rest and Relax (Wilderness)",
                "Any",
                "Focused Attention",
                "General",
                "Abandon Hex",
                "Build Structure",
                "Claim Hex",
                "Clear Hex",
                "Establish Settlement",
                "Establish Trade Agreement",
                "New Leadership",
                "Pledge of Fealty",
                "Quell Unrest",
                "Repair Reputation",
                "Rest and Relax",
                "Army",
                "Recover Army",
                "Recruit Army",
                "Train Army",
            ),
            settlementDetailsMatrixRows.map { it.label },
        )
    }

    @Test
    fun calculatesSkillAndActivityBonusesForMatrixRows() {
        val settlement = settlementWithBonuses(
            GroupedStructureBonus(
                structureNames = setOf("Farm"),
                skill = KingdomSkill.AGRICULTURE,
                activity = null,
                value = 2,
                locatedIn = "Tusk Hold",
            ),
            GroupedStructureBonus(
                structureNames = setOf("Theater"),
                skill = KingdomSkill.ARTS,
                activity = null,
                value = 1,
                locatedIn = "Tusk Hold",
            ),
            GroupedStructureBonus(
                structureNames = setOf("Arena"),
                skill = null,
                activity = "rest-and-relax",
                value = 2,
                locatedIn = "Tusk Hold",
            ),
            GroupedStructureBonus(
                structureNames = setOf("Museum"),
                skill = KingdomSkill.ARTS,
                activity = "rest-and-relax",
                value = 3,
                locatedIn = "Tusk Hold",
            ),
            GroupedStructureBonus(
                structureNames = setOf("Yard"),
                skill = KingdomSkill.ENGINEERING,
                activity = "build-structure",
                value = 4,
                locatedIn = "Tusk Hold",
            ),
        )
        val rows = settlementDetailsMatrixRows.associateBy { it.label }

        assertEquals(2, settlement.matrixBonusFor(rows.getValue("Agriculture")))
        assertEquals(2, settlement.matrixBonusFor(rows.getValue("Establish Farmland")))
        assertEquals(1, settlement.matrixBonusFor(rows.getValue("Arts")))
        assertEquals(3, settlement.matrixBonusFor(rows.getValue("Rest and Relax (Arts)")))
        assertEquals(3, settlement.matrixBonusFor(rows.getValue("Rest and Relax")))
        assertEquals(4, settlement.matrixBonusFor(rows.getValue("Build Structure")))
        assertNull(settlement.matrixBonusFor(rows.getValue("Any")))
    }

    private fun settlementWithBonuses(vararg bonuses: GroupedStructureBonus) = Settlement(
        id = "tusk-hold",
        name = "Tusk Hold",
        type = SettlementType.CAPITAL,
        waterBorders = 0,
        isSecondaryTerritory = false,
        settlementEventBonus = 0,
        leaderLeadershipActivityBonus = 0,
        bonuses = bonuses.toSet(),
        allowCapitalInvestment = true,
        notes = emptySet(),
        storage = CommodityStorage(),
        increaseLeadershipActivities = false,
        consumptionReduction = 0,
        availableItems = AvailableItemBonuses(),
        size = settlementSizeData.first(),
        unlockActivities = emptySet(),
        residentialLots = 0,
        hasBridge = false,
        occupiedBlocks = 1,
        preventItemLevelPenalty = false,
        delayedStructures = emptyList(),
        constructedStructures = emptyList(),
        structuresUnderConstruction = emptyList(),
        maximumCivicRdLimit = 0,
        settlementActions = 0,
        blocks = emptyList(),
        layoutType = SettlementLayoutType.RIGID,
    )
}
