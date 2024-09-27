package at.posselt.pfrpg2e.app.camping

import at.posselt.pfrpg2e.camping.calculateCharges
import kotlin.test.Test
import kotlin.test.assertEquals

class FoodTest {
    @Test
    fun testChargesReductionMoreThanPossible() {
        val result = calculateCharges(removeQuantity = 2, itemQuantity = 1, itemMaxUses = 13, itemUses = 1)
        assertEquals(0, result.quantity)
        assertEquals(0, result.charges)
    }

    @Test
    fun testChargesReductionSimple() {
        val result = calculateCharges(removeQuantity = 1, itemQuantity = 1, itemMaxUses = 13, itemUses = 13)
        assertEquals(1, result.quantity)
        assertEquals(12, result.charges)
    }

    @Test
    fun testChargesReductionWraps() {
        val result = calculateCharges(removeQuantity = 1, itemQuantity = 2, itemMaxUses = 13, itemUses = 1)
        assertEquals(1, result.quantity)
        assertEquals(13, result.charges)
    }

    @Test
    fun testChargesReductionWrapsRemoveAll() {
        val result = calculateCharges(removeQuantity = 13, itemQuantity = 2, itemMaxUses = 13, itemUses = 13)
        assertEquals(1, result.quantity)
        assertEquals(13, result.charges)
    }

    @Test
    fun testChargesReductionWrapsRemoveAllMoreThanOnce() {
        val result = calculateCharges(removeQuantity = 13, itemQuantity = 3, itemMaxUses = 6, itemUses = 5)
        assertEquals(1, result.quantity)
        assertEquals(4, result.charges)
    }
}