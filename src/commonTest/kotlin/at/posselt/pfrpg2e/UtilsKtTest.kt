package at.posselt.pfrpg2e

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UtilsKtTest {
    @Test
    fun deCamelCase() {
        assertEquals("Test", "test".deCamelCase())
        assertEquals("Test It", "testIt".deCamelCase())
        assertEquals("", "".deCamelCase())
    }

    @Test
    fun enumOrdinal() {
        assertEquals(DegreeOfSuccess.CRITICAL_FAILURE, fromOrdinal<DegreeOfSuccess>(0))
        assertEquals(DegreeOfSuccess.FAILURE, fromOrdinal<DegreeOfSuccess>(1))
        assertEquals(DegreeOfSuccess.SUCCESS, fromOrdinal<DegreeOfSuccess>(2))
        assertEquals(DegreeOfSuccess.CRITICAL_SUCCESS, fromOrdinal<DegreeOfSuccess>(3))
        assertNull(fromOrdinal<DegreeOfSuccess>(4))
    }

    @Test
    fun enumToCamelCase() {
        assertEquals("criticalFailure", DegreeOfSuccess.CRITICAL_FAILURE.toCamelCase())
        assertEquals("failure", DegreeOfSuccess.FAILURE.toCamelCase())
    }

    @Test
    fun enumToLabel() {
        assertEquals("Critical Failure", DegreeOfSuccess.CRITICAL_FAILURE.toLabel())
        assertEquals("Failure", DegreeOfSuccess.FAILURE.toLabel())
    }

    @Test
    fun slugify() {
        assertEquals("critical-failure", "Critical Failure".slugify())
        assertEquals("critical-failure", " Critical  Failure".slugify())
        assertEquals("failure", "Failure".slugify())
    }

    @Test
    fun unslugify() {
        assertEquals("Critical Failure", "critical-failure".unslugify())
        assertEquals("Failure", "failure".unslugify())
    }
}