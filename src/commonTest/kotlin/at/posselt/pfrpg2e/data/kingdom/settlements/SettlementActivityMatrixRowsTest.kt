package at.posselt.pfrpg2e.data.kingdom.settlements

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettlementActivityMatrixRowsTest {
    @Test
    fun workbookSettlementRowsA46ThroughA145AreRepresentedInOrder() {
        assertEquals(82, settlementActivityMatrixRows.size)
        assertEquals("Agriculture", settlementActivityMatrixRows.first().label)
        assertEquals(46, settlementActivityMatrixRows.first().workbookRow)
        assertTrue(settlementActivityMatrixRows.first().isHeader)
        assertEquals(KingdomSkill.AGRICULTURE, settlementActivityMatrixRows.first().skill)
        assertNull(settlementActivityMatrixRows.first().activityId)

        assertEquals("Train Army", settlementActivityMatrixRows.last().label)
        assertEquals(145, settlementActivityMatrixRows.last().workbookRow)
        assertFalse(settlementActivityMatrixRows.last().isHeader)
        assertEquals("train-army", settlementActivityMatrixRows.last().activityId)
    }

    @Test
    fun workbookSkillAndActivityRowsKeepTheirSettlementMatrixMappings() {
        val restAndRelaxArts = settlementActivityMatrixRows.single { it.workbookRow == 54 }
        assertEquals("Rest and Relax (Arts)", restAndRelaxArts.label)
        assertEquals(KingdomSkill.ARTS, restAndRelaxArts.skill)
        assertEquals("rest-and-relax", restAndRelaxArts.activityId)

        val establishWorkSite = settlementActivityMatrixRows.single { it.workbookRow == 68 }
        assertEquals("Establish Work Site", establishWorkSite.label)
        assertEquals("establish-work-site", establishWorkSite.activityId)

        val lumberCamp = settlementActivityMatrixRows.single { it.workbookRow == 69 }
        assertEquals("Establish Work Site (Lumber Camp)", lumberCamp.label)
        assertEquals("establish-work-site-lumber", lumberCamp.activityId)

        val anyHeader = settlementActivityMatrixRows.single { it.workbookRow == 126 }
        assertEquals("Any", anyHeader.label)
        assertTrue(anyHeader.isHeader)
        assertNull(anyHeader.skill)
        assertNull(anyHeader.activityId)

        val focusedAttention = settlementActivityMatrixRows.single { it.workbookRow == 127 }
        assertEquals("focused-attention", focusedAttention.activityId)
        assertNull(focusedAttention.skill)

        val armyHeader = settlementActivityMatrixRows.single { it.workbookRow == 142 }
        assertEquals("Army", armyHeader.label)
        assertTrue(armyHeader.isHeader)
        assertNull(armyHeader.skill)
        assertNull(armyHeader.activityId)
    }
}
