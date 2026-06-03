package at.posselt.pfrpg2e.data.kingdom.leaders

import at.posselt.pfrpg2e.data.kingdom.KingdomAbility
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaderVacancyPenaltyTest {

	@Test
	fun rulerVacancyPenaltyMatchesWorkbook() {
		assertEquals(-1, Leader.RULER.vacancyPenalty, "Ruler vacancy penalty should be -1")
	}

	@Test
	fun counselorVacancyPenaltyMatchesWorkbook() {
		assertEquals(-1, Leader.COUNSELOR.vacancyPenalty, "Counselor vacancy penalty should be -1")
	}

	@Test
	fun generalVacancyPenaltyMatchesWorkbook() {
		assertEquals(-4, Leader.GENERAL.vacancyPenalty, "General vacancy penalty should be -4")
	}

	@Test
	fun emissaryVacancyPenaltyMatchesWorkbook() {
		assertEquals(-1, Leader.EMISSARY.vacancyPenalty, "Emissary vacancy penalty should be -1")
	}

	@Test
	fun magisterVacancyPenaltyMatchesWorkbook() {
		assertEquals(-4, Leader.MAGISTER.vacancyPenalty, "Magister vacancy penalty should be -4")
	}

	@Test
	fun treasurerVacancyPenaltyMatchesWorkbook() {
		assertEquals(-1, Leader.TREASURER.vacancyPenalty, "Treasurer vacancy penalty should be -1")
	}

	@Test
	fun viceroyVacancyPenaltyMatchesWorkbook() {
		assertEquals(-1, Leader.VICEROY.vacancyPenalty, "Viceroy vacancy penalty should be -1")
	}

	@Test
	fun wardenVacancyPenaltyMatchesWorkbook() {
		assertEquals(-4, Leader.WARDEN.vacancyPenalty, "Warden vacancy penalty should be -4")
	}

	@Test
	fun allVacancyPenaltiesAreNegative() {
		Leader.entries.forEach {
			assertTrue(it.vacancyPenalty < 0, "Leader ${it.name} vacancy penalty should be negative, got ${it.vacancyPenalty}")
		}
	}

	@Test
	fun leadershipRolesWithMilitarySkillsHaveHigherPenalty() {
		assertEquals(-4, Leader.GENERAL.vacancyPenalty)
		assertEquals(-4, Leader.WARDEN.vacancyPenalty)
	}

	@Test
	fun leadershipRolesWithDiplomacySkillsHaveLowerPenalty() {
		assertEquals(-1, Leader.RULER.vacancyPenalty)
		assertEquals(-1, Leader.EMISSARY.vacancyPenalty)
		assertEquals(-1, Leader.COUNSELOR.vacancyPenalty)
		assertEquals(-1, Leader.TREASURER.vacancyPenalty)
		assertEquals(-1, Leader.VICEROY.vacancyPenalty)
	}

	@Test
	fun keyAbilityMatchesVacancyPenalty() {
		assertEquals(KingdomAbility.LOYALTY, Leader.RULER.keyAbility)
		assertEquals(KingdomAbility.CULTURE, Leader.COUNSELOR.keyAbility)
		assertEquals(KingdomAbility.STABILITY, Leader.GENERAL.keyAbility)
		assertEquals(KingdomAbility.LOYALTY, Leader.EMISSARY.keyAbility)
		assertEquals(KingdomAbility.CULTURE, Leader.MAGISTER.keyAbility)
		assertEquals(KingdomAbility.ECONOMY, Leader.TREASURER.keyAbility)
		assertEquals(KingdomAbility.ECONOMY, Leader.VICEROY.keyAbility)
		assertEquals(KingdomAbility.STABILITY, Leader.WARDEN.keyAbility)
	}
}
