package at.posselt.pfrpg2e.data.checks

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess.*
import kotlin.test.Test
import kotlin.test.assertEquals

class DegreeOfSuccessTest {
    @Test
    fun degreeOfSuccess() {
        assertEquals(CRITICAL_FAILURE, determineDegreeOfSuccess(10, 0, dieValue = 2))
        assertEquals(CRITICAL_FAILURE, determineDegreeOfSuccess(10, 9, dieValue = 1))
        assertEquals(FAILURE, determineDegreeOfSuccess(10, 0, dieValue = 20))
        assertEquals(FAILURE, determineDegreeOfSuccess(10, 9, dieValue = 2))
        assertEquals(FAILURE, determineDegreeOfSuccess(10, 10, dieValue = 1))
        assertEquals(SUCCESS, determineDegreeOfSuccess(10, 10, dieValue = 2))
        assertEquals(SUCCESS, determineDegreeOfSuccess(10, 20, dieValue = 1))
        assertEquals(CRITICAL_SUCCESS, determineDegreeOfSuccess(10, 10, dieValue = 20))
        assertEquals(CRITICAL_SUCCESS, determineDegreeOfSuccess(10, 20, dieValue = 2))
    }
}