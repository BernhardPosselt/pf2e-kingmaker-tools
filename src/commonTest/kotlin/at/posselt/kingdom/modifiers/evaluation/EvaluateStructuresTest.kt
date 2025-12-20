package at.posselt.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.settlements.SettlementSizeType
import at.posselt.pfrpg2e.data.kingdom.settlements.SettlementType
import at.posselt.pfrpg2e.data.kingdom.settlements.settlementSizeData
import at.posselt.pfrpg2e.data.kingdom.structures.AvailableItemsRule
import at.posselt.pfrpg2e.data.kingdom.structures.CommodityStorage
import at.posselt.pfrpg2e.data.kingdom.structures.GroupedStructureBonus
import at.posselt.pfrpg2e.data.kingdom.structures.ItemGroup
import at.posselt.pfrpg2e.data.kingdom.structures.Structure
import at.posselt.pfrpg2e.data.kingdom.structures.StructureBonus
import at.posselt.pfrpg2e.data.kingdom.structures.StructureTrait
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.SettlementData
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.evaluateSettlement
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.parseAvailableItems
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
                id = "stackwith",
                stacksWith = "residential",
                consumptionReduction = 1,
                uuid = "",
                actorUuid = "",
                bonuses = setOf(
                    StructureBonus(
                        skill = KingdomSkill.AGRICULTURE,
                        activity = null,
                        value = 1
                    ),
                )
            ),
            Structure(
                name = "residential",
                id = "residential",
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
                uuid = "",
                actorUuid = "",
                bonuses = setOf(
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
                id = "residential",
                lots = 1,
                traits = setOf(StructureTrait.RESIDENTIAL),
                settlementEventBonus = 1,
                leadershipActivityBonus = 2,
                notes = "note2",
                storage = CommodityStorage(stone = 1, lumber = 1),
                consumptionReduction = 1,
                uuid = "",
                actorUuid = "",
                bonuses = setOf(
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
        val result = evaluateSettlement(
            data = SettlementData(
                name = "name",
                id = "name",
                type = SettlementType.CAPITAL,
                waterBorders = 4,
                occupiedBlocks = 20,
                isSecondaryTerritory = true,
            ),
            structures = structures,
            allStructuresStack = false,
            allowCapitalInvestmentInCapitalWithoutBank = false,
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
        assertEquals(20, result.occupiedBlocks)
        assertTrue(result.increaseLeadershipActivities)
        assertEquals(settlementSizeData.find { it.type == SettlementSizeType.METROPOLIS }, result.size)
        assertEquals(3, result.residentialLots)
        assertEquals(setOf("note1", "note2"), result.notes)
        assertEquals(setOf("something"), result.unlockActivities)
        assertEquals(CommodityStorage(stone = 1, lumber = 2, ore = 1), result.storage)
        assertEquals(1, result.consumptionReduction)
        assertEquals(0, result.consumptionSurplus)
        assertEquals(5, result.consumption)
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
        val result = evaluateSettlement(
            data = SettlementData(
                name = "name",
                id = "name",
                type = SettlementType.CAPITAL,
                waterBorders = 4,
                occupiedBlocks = 4,
                isSecondaryTerritory = true,
            ),
            structures = emptyList(),
            allStructuresStack = false,
            allowCapitalInvestmentInCapitalWithoutBank = false,
        )
        assertEquals(4, result.waterBorders)
    }

    @Test
    fun allStructuresStack() {
        val result = evaluateSettlement(
            data = SettlementData(
                name = "name",
                id = "name",
                type = SettlementType.CAPITAL,
                waterBorders = 3,
                occupiedBlocks = 14,
                isSecondaryTerritory = true,
            ),
            structures = listOf(
                Structure(
                    name = "residential",
                    id = "residential",
                    uuid = "",
                    actorUuid = "",
                    bonuses = setOf(
                        StructureBonus(
                            skill = KingdomSkill.AGRICULTURE,
                            activity = null,
                            value = 1
                        ),
                    )
                ),
                Structure(
                    name = "other",
                    id = "other",
                    uuid = "",
                    actorUuid = "",
                    bonuses = setOf(
                        StructureBonus(
                            skill = KingdomSkill.AGRICULTURE,
                            activity = null,
                            value = 1
                        ),
                    )
                ),
            ),
            allStructuresStack = true,
            allowCapitalInvestmentInCapitalWithoutBank = false,
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
        val result = evaluateSettlement(
            data = SettlementData(
                name = "name",
                id = "name",
                type = SettlementType.CAPITAL,
                waterBorders = 3,
                occupiedBlocks = 4,
                isSecondaryTerritory = true,
            ),
            structures = listOf(
                Structure(
                    name = "residential",
                    id = "residential",
                    consumptionReduction = 1,
                    consumptionReductionStacks = true,
                    uuid = "",
                    actorUuid = ""
                ),
                Structure(
                    name = "residential",
                    id = "residential",
                    consumptionReduction = 1,
                    consumptionReductionStacks = true,
                    uuid = "",
                    actorUuid = ""
                ),
            ),
            allStructuresStack = false,
            allowCapitalInvestmentInCapitalWithoutBank = false,
        )
        assertEquals(2, result.consumptionReduction)
    }

    @Test
    fun consumptionStackDisabling() {
        val result = evaluateSettlement(
            data = SettlementData(
                name = "name",
                id = "name",
                type = SettlementType.CAPITAL,
                waterBorders = 3,
                occupiedBlocks = 4,
                isSecondaryTerritory = true,
            ),
            structures = listOf(
                Structure(
                    name = "residential",
                    id = "residential",
                    consumptionReduction = 1,
                    consumptionReductionStacks = true,
                    uuid = "",
                    actorUuid = ""
                ),
                Structure(
                    name = "other",
                    id = "other",
                    consumptionReduction = 3,
                    ignoreConsumptionReductionOf = setOf("residential"),
                    uuid = "",
                    actorUuid = ""
                ),
            ),
            allStructuresStack = false,
            allowCapitalInvestmentInCapitalWithoutBank = false,
        )
        assertEquals(3, result.consumptionReduction)
    }

    @Test
    fun consumptionStackWith() {
        val result = evaluateSettlement(
            data = SettlementData(
                name = "name",
                id = "name",
                type = SettlementType.CAPITAL,
                waterBorders = 3,
                occupiedBlocks = 4,
                isSecondaryTerritory = true,
            ),
            structures = listOf(
                Structure(
                    name = "residential",
                    id = "residential",
                    consumptionReduction = 1,
                    uuid = "",
                    actorUuid = ""
                ),
                Structure(
                    stacksWith = "residential",
                    name = "other",
                    id = "other",
                    consumptionReduction = 1,
                    uuid = "",
                    actorUuid = ""
                ),
            ),
            allStructuresStack = false,
            allowCapitalInvestmentInCapitalWithoutBank = false,
        )
        assertEquals(1, result.consumptionReduction)
    }

    @Test
    fun shouldSumItemBonuses() {
        val result = parseAvailableItems(
            listOf(
                Structure(
                    name = "other",
                    id = "other",
                    uuid = "",
                    actorUuid = "",
                    availableItemsRules = setOf(AvailableItemsRule(
                        value = 1,
                        group = ItemGroup.DIVINE,
                        maximumStacks = 3
                    ))
                ),
            )
        )
        assertEquals(1, result.divine)
    }

    @Test
    fun itemBonusesFromTheSameBuildingStackUpToMaximumStacks() {
        val result = parseAvailableItems(
            listOf(
                Structure(
                    name = "other",
                    id = "other",
                    uuid = "",
                    actorUuid = "",
                    availableItemsRules = setOf(AvailableItemsRule(
                        value = 1,
                        group = ItemGroup.DIVINE,
                        maximumStacks = 2
                    ))
                ),
                Structure(
                    name = "other",
                    id = "other",
                    uuid = "",
                    actorUuid = "",
                    availableItemsRules = setOf(AvailableItemsRule(
                        value = 1,
                        group = ItemGroup.DIVINE,
                        maximumStacks = 2
                    ))
                ),
                Structure(
                    name = "other",
                    id = "other",
                    uuid = "",
                    actorUuid = "",
                    availableItemsRules = setOf(AvailableItemsRule(
                        value = 1,
                        group = ItemGroup.DIVINE,
                        maximumStacks = 2
                    ))
                ),
            )
        )
        assertEquals(2, result.divine)
    }

    @Test
    fun onlyHighestItemBonusPerTypeStacks() {
        val result = parseAvailableItems(
            listOf(
                Structure(
                    name = "other1",
                    id = "other1",
                    uuid = "",
                    actorUuid = "",
                    availableItemsRules = setOf(AvailableItemsRule(
                        value = 3,
                        group = ItemGroup.DIVINE,
                        maximumStacks = 2
                    ))
                ),
                Structure(
                    name = "other1",
                    id = "other1",
                    uuid = "",
                    actorUuid = "",
                    availableItemsRules = setOf(AvailableItemsRule(
                        value = 3,
                        group = ItemGroup.DIVINE,
                        maximumStacks = 2
                    ))
                ),
                Structure(
                    name = "other",
                    id = "other",
                    uuid = "",
                    actorUuid = "",
                    availableItemsRules = setOf(AvailableItemsRule(
                        value = 1,
                        group = ItemGroup.DIVINE,
                        maximumStacks = 2
                    ))
                ),
                Structure(
                    name = "other",
                    id = "other",
                    uuid = "",
                    actorUuid = "",
                    availableItemsRules = setOf(AvailableItemsRule(
                        value = 1,
                        group = ItemGroup.DIVINE,
                        maximumStacks = 2
                    ))
                ),
                Structure(
                    name = "other",
                    id = "other",
                    uuid = "",
                    actorUuid = "",
                    availableItemsRules = setOf(AvailableItemsRule(
                        value = 1,
                        group = ItemGroup.DIVINE,
                        maximumStacks = 2
                    ))
                ),
            )
        )
        assertEquals(6, result.divine)
    }

    @Test
    fun nullMaximumStacksDefaultsTo3() {
        val result = parseAvailableItems(
            listOf(
                Structure(
                    name = "other",
                    id = "other",
                    uuid = "",
                    actorUuid = "",
                    availableItemsRules = setOf(AvailableItemsRule(
                        value = 1,
                        group = ItemGroup.DIVINE,
                    ))
                ),
                Structure(
                    name = "other",
                    id = "other",
                    uuid = "",
                    actorUuid = "",
                    availableItemsRules = setOf(AvailableItemsRule(
                        value = 1,
                        group = ItemGroup.DIVINE,
                    ))
                ),
                Structure(
                    name = "other",
                    id = "other",
                    uuid = "",
                    actorUuid = "",
                    availableItemsRules = setOf(AvailableItemsRule(
                        value = 1,
                        group = ItemGroup.DIVINE,
                    ))
                ),
                Structure(
                    name = "other",
                    id = "other",
                    uuid = "",
                    actorUuid = "",
                    availableItemsRules = setOf(AvailableItemsRule(
                        value = 1,
                        group = ItemGroup.DIVINE,
                    ))
                ),
            )
        )
        assertEquals(3, result.divine)
    }

    @Test
    fun alwaysStacksStacks() {
        val result = parseAvailableItems(
            listOf(
                Structure(
                    name = "other",
                    id = "other",
                    uuid = "",
                    actorUuid = "",
                    availableItemsRules = setOf(AvailableItemsRule(
                        value = 1,
                        group = ItemGroup.DIVINE,
                        alwaysStacks = true,
                        maximumStacks = 1,
                    ))
                ),
                Structure(
                    name = "other",
                    id = "other",
                    uuid = "",
                    actorUuid = "",
                    availableItemsRules = setOf(AvailableItemsRule(
                        value = 1,
                        group = ItemGroup.DIVINE,
                        alwaysStacks = true,
                        maximumStacks = 1,
                    ))
                ),
                Structure(
                    name = "other1",
                    id = "other1",
                    uuid = "",
                    actorUuid = "",
                    availableItemsRules = setOf(AvailableItemsRule(
                        value = 1,
                        group = ItemGroup.DIVINE,
                        maximumStacks = 1,
                    ))
                ),
                Structure(
                    name = "other1",
                    id = "other1",
                    uuid = "",
                    actorUuid = "",
                    availableItemsRules = setOf(AvailableItemsRule(
                        value = 1,
                        group = ItemGroup.DIVINE,
                        maximumStacks = 1,
                    ))
                ),
            )
        )
        assertEquals(2, result.divine)
    }
}