package at.posselt.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.settlements.SettlementType
import at.posselt.pfrpg2e.data.kingdom.settlements.settlementSizeData
import at.posselt.pfrpg2e.data.kingdom.structures.CommodityStorage
import at.posselt.pfrpg2e.data.kingdom.structures.GroupedStructureBonus
import at.posselt.pfrpg2e.data.kingdom.structures.Structure
import at.posselt.pfrpg2e.data.kingdom.structures.StructureBonus
import at.posselt.pfrpg2e.data.kingdom.structures.StructureTrait
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.evaluateStructures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EvaluateStructuresTest {
    @Test
    fun basic() {
        val structures = listOf(
            Structure(
                name = "residential",
                lots = 2,
                enableCapitalInvestment = true,
                traits = setOf(StructureTrait.RESIDENTIAL),
                settlementEventBonus = 2,
                leadershipActivityBonus = 1,
                increaseLeadershipActivities = true,
                notes = "note1",
                unlockActivities = setOf("something"),
                storage = CommodityStorage(ore = 1, lumber = 1),
                consumptionReduction = 1,
                bonuses = listOf(
                    StructureBonus(
                        skill = KingdomSkill.AGRICULTURE,
                        activity = null,
                        value = 1
                    )
                )
            ),
            Structure(
                name = "residential",
                lots = 1,
                traits = setOf(StructureTrait.RESIDENTIAL),
                settlementEventBonus = 1,
                leadershipActivityBonus = 2,
                notes = "note2",
                storage = CommodityStorage(stone = 1, lumber = 1),
                consumptionReduction = 1,
                bonuses = listOf(
                    StructureBonus(
                        skill = KingdomSkill.AGRICULTURE,
                        activity = null,
                        value = 1
                    )
                )
            ),
        )
        val result = evaluateStructures(
            settlementName = "name",
            settlementLevel = 20,
            waterBorders = 4,
            occupiedBlocks = 4,
            isSecondaryTerritory = true,
            structures = structures,
            allStructuresStack = false,
        )
        assertEquals("name", result.name)
        assertEquals(4, result.waterBorders)
        assertTrue(result.lacksBridge)
        assertTrue(result.isOvercrowded)
        assertTrue(result.isSecondaryTerritory)
        assertTrue(result.allowCapitalInvestment)
        assertEquals(3, result.settlementEventBonus)
        assertEquals(2, result.leaderLeadershipActivityBonus)
        assertFalse(result.hasBridge)
        assertEquals(4, result.occupiedBlocks)
        assertTrue(result.increaseLeadershipActivities)
        assertEquals(settlementSizeData.find { it.type == SettlementType.METROPOLIS }, result.settlementSize)
        assertEquals(3, result.residentialLots)
        assertEquals(setOf("note1", "note2"), result.notes)
        assertEquals(setOf("something"), result.unlockActivities)
        assertEquals(CommodityStorage(stone = 1, lumber = 2, ore = 1), result.storage)
        assertEquals(1, result.consumptionReduction)
        assertEquals(0, result.consumptionSurplus)
        assertEquals(5, result.settlementConsumption)
        assertEquals(
            setOf(
                GroupedStructureBonus(
                    value = 2,
                    skill = KingdomSkill.AGRICULTURE,
                    structureNames = setOf("residential"),
                    activity = null,
                )
            ), result.bonuses
        )
    }
}