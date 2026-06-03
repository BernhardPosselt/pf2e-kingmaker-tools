package at.posselt.pfrpg2e.data.kingdom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CharterTest {

	@Test
	fun rowCountMatchesWorkbook() {
		assertEquals(5, charters.size, "Workbook has exactly 5 charters")
	}

	@Test
	fun conquestMatchesWorkbook() {
		val conquest = charters.find { it.name == "Conquest" }
		assertNotNull(conquest)
		assertEquals(KingdomAbility.LOYALTY, conquest.boost)
		assertEquals(KingdomAbility.CULTURE, conquest.flaw)
		assertTrue(conquest.description.contains("conquered an area"))
	}

	@Test
	fun expansionMatchesWorkbook() {
		val expansion = charters.find { it.name == "Expansion" }
		assertNotNull(expansion)
		assertEquals(KingdomAbility.CULTURE, expansion.boost)
		assertEquals(KingdomAbility.STABILITY, expansion.flaw)
		assertTrue(expansion.description.contains("adjacent to already settled lands"))
	}

	@Test
	fun explorationMatchesWorkbook() {
		val exploration = charters.find { it.name == "Exploration" }
		assertNotNull(exploration)
		assertEquals(KingdomAbility.STABILITY, exploration.boost)
		assertEquals(KingdomAbility.ECONOMY, exploration.flaw)
		assertTrue(exploration.description.contains("explore, clear, and settle"))
	}

	@Test
	fun grantMatchesWorkbook() {
		val grant = charters.find { it.name == "Grant" }
		assertNotNull(grant)
		assertEquals(KingdomAbility.ECONOMY, grant.boost)
		assertEquals(KingdomAbility.LOYALTY, grant.flaw)
		assertTrue(grant.description.contains("large amount of funding"))
	}

	@Test
	fun openCharterHasNoBoostOrFlaw() {
		val open = charters.find { it.name == "Open" }
		assertNotNull(open)
		assertNull(open.boost, "Open charter should have no fixed boost")
		assertNull(open.flaw, "Open charter should have no fixed flaw")
		assertTrue(open.description.contains("no restrictions"))
		assertTrue(open.description.contains("no direct support"))
	}

	@Test
	fun allCharterNamesAreUnique() {
		val names = charters.map { it.name }
		assertEquals(names.size, names.toSet().size, "All charter names should be unique")
	}

	@Test
	fun allCharterDescriptionsAreNonBlank() {
		charters.forEach {
			assertFalse(it.description.isBlank(), "Charter ${it.name} description should not be blank")
		}
	}

	@Test
	fun findCharterReturnsCorrectCharter() {
		val result = findCharter("Conquest")
		assertNotNull(result)
		assertEquals("Conquest", result.name)
		assertEquals(KingdomAbility.LOYALTY, result.boost)
	}

	@Test
	fun findCharterIsCaseInsensitive() {
		val result = findCharter("conquest")
		assertNotNull(result)
		assertEquals("Conquest", result.name)
	}

	@Test
	fun findCharterReturnsNullForInvalidName() {
		assertNull(findCharter("Nonexistent Charter"))
	}
}
