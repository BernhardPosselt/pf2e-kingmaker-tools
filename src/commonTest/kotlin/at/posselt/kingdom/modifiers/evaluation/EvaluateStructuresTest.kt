package at.posselt.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.settlements.SettlementSizeType
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
    fun full() {
        val structures = listOf(
            Structure(
                name = "stackwith",
                stacksWith = "residential",
                consumptionReduction = 1,
                bonuses = listOf(
                    StructureBonus(
                        skill = KingdomSkill.AGRICULTURE,
                        activity = null,
                        value = 1
                    ),
                )
            ),
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
                    ),
                    StructureBonus(
                        skill = KingdomSkill.AGRICULTURE,
                        activity = "establish-farmland",
                        value = 1
                    ),
                    StructureBonus(
                        skill = KingdomSkill.AGRICULTURE,
                        activity = "establish-something-else",
                        value = 2
                    ),
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
                    ),
                    StructureBonus(
                        skill = KingdomSkill.AGRICULTURE,
                        activity = "establish-something-else",
                        value = 2
                    ),
                )
            ),

            )
        val result = evaluateStructures(
            settlementName = "name",
            settlementLevel = 20,
            settlementType = SettlementType.CAPITAL,
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
        assertEquals(settlementSizeData.find { it.type == SettlementSizeType.METROPOLIS }, result.settlementSize)
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
                    value = 3,
                    skill = KingdomSkill.AGRICULTURE,
                    structureNames = setOf("residential"),
                    activity = null,
                    locatedIn = "name",
                ),
                GroupedStructureBonus(
                    value = 1,
                    skill = KingdomSkill.AGRICULTURE,
                    structureNames = setOf("residential"),
                    activity = "establish-farmland",
                    locatedIn = "name",
                ),
                GroupedStructureBonus(
                    value = 3,
                    skill = KingdomSkill.AGRICULTURE,
                    structureNames = setOf("residential"),
                    activity = "establish-something-else",
                    locatedIn = "name",
                ),
            ), result.bonuses
        )
    }

    @Test
    fun shouldNotFailIfNoStructures() {
        val result = evaluateStructures(
            settlementName = "name",
            settlementLevel = 20,
            waterBorders = 4,
            occupiedBlocks = 4,
            isSecondaryTerritory = true,
            structures = emptyList(),
            allStructuresStack = false,
            settlementType = SettlementType.CAPITAL,
        )
        assertEquals(4, result.waterBorders)
    }

    @Test
    fun allStructuresStack() {
        val result = evaluateStructures(
            settlementName = "name",
            settlementLevel = 14,
            waterBorders = 3,
            occupiedBlocks = 4,
            isSecondaryTerritory = true,
            structures = listOf(
                Structure(
                    name = "residential",
                    bonuses = listOf(
                        StructureBonus(
                            skill = KingdomSkill.AGRICULTURE,
                            activity = null,
                            value = 1
                        ),
                    )
                ),
                Structure(
                    name = "other",
                    bonuses = listOf(
                        StructureBonus(
                            skill = KingdomSkill.AGRICULTURE,
                            activity = null,
                            value = 1
                        ),
                    )
                ),
            ),
            allStructuresStack = true,
            settlementType = SettlementType.CAPITAL,
        )
        assertEquals(
            setOf(
                GroupedStructureBonus(
                    value = 2,
                    skill = KingdomSkill.AGRICULTURE,
                    structureNames = setOf("residential", "other"),
                    activity = null,
                    locatedIn = "name",
                ),
            ), result.bonuses
        )
    }

    @Test
    fun consumptionStacking() {
        val result = evaluateStructures(
            settlementName = "name",
            settlementLevel = 14,
            waterBorders = 3,
            occupiedBlocks = 4,
            isSecondaryTerritory = true,
            structures = listOf(
                Structure(
                    name = "residential",
                    consumptionReduction = 1,
                    consumptionReductionStacks = true,
                ),
                Structure(
                    name = "residential",
                    consumptionReduction = 1,
                    consumptionReductionStacks = true,
                ),
            ),
            allStructuresStack = false,
            settlementType = SettlementType.CAPITAL,
        )
        assertEquals(2, result.consumptionReduction)
    }

    @Test
    fun consumptionStackDisabling() {
        val result = evaluateStructures(
            settlementName = "name",
            settlementLevel = 14,
            waterBorders = 3,
            occupiedBlocks = 4,
            isSecondaryTerritory = true,
            structures = listOf(
                Structure(
                    name = "residential",
                    consumptionReduction = 1,
                    consumptionReductionStacks = true,
                ),
                Structure(
                    name = "other",
                    consumptionReduction = 3,
                    ignoreConsumptionReductionOf = setOf("residential")
                ),
            ),
            allStructuresStack = false,
            settlementType = SettlementType.CAPITAL,
        )
        assertEquals(3, result.consumptionReduction)
    }

    @Test
    fun consumptionStackWith() {
        val result = evaluateStructures(
            settlementName = "name",
            settlementLevel = 14,
            waterBorders = 3,
            occupiedBlocks = 4,
            isSecondaryTerritory = true,
            structures = listOf(
                Structure(
                    name = "residential",
                    consumptionReduction = 1,
                ),
                Structure(
                    stacksWith = "residential",
                    name = "other",
                    consumptionReduction = 1,
                ),
            ),
            allStructuresStack = false,
            settlementType = SettlementType.CAPITAL,
        )
        assertEquals(1, result.consumptionReduction)
    }

}