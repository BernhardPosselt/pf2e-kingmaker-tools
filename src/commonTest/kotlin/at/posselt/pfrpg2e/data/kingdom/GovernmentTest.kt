package at.posselt.pfrpg2e.data.kingdom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GovernmentTest {

	@Test
	fun rowCountMatchesWorkbook() {
		assertEquals(6, governments.size, "Workbook has exactly 6 government types")
	}

	@Test
	fun despotismMatchesWorkbook() {
		val despotism = governments.find { it.name == "Despotism" }
		assertNotNull(despotism)
		assertEquals(setOf(KingdomAbility.STABILITY, KingdomAbility.ECONOMY), despotism.abilityBoosts)
		assertEquals(setOf(KingdomSkill.INTRIGUE, KingdomSkill.WARFARE), despotism.skillProficiencies)
		assertEquals("Crush Dissent", despotism.bonusFeat)
	}

	@Test
	fun feudalismMatchesWorkbook() {
		val feudalism = governments.find { it.name == "Feudalism" }
		assertNotNull(feudalism)
		assertEquals(setOf(KingdomAbility.STABILITY, KingdomAbility.CULTURE), feudalism.abilityBoosts)
		assertEquals(setOf(KingdomSkill.DEFENSE, KingdomSkill.TRADE), feudalism.skillProficiencies)
		assertEquals("Fortified Fiefs", feudalism.bonusFeat)
	}

	@Test
	fun oligarchyMatchesWorkbook() {
		val oligarchy = governments.find { it.name == "Oligarchy" }
		assertNotNull(oligarchy)
		assertEquals(setOf(KingdomAbility.LOYALTY, KingdomAbility.ECONOMY), oligarchy.abilityBoosts)
		assertEquals(setOf(KingdomSkill.ARTS, KingdomSkill.INDUSTRY), oligarchy.skillProficiencies)
		assertEquals("Insider Trading", oligarchy.bonusFeat)
	}

	@Test
	fun republicMatchesWorkbook() {
		val republic = governments.find { it.name == "Republic" }
		assertNotNull(republic)
		assertEquals(setOf(KingdomAbility.STABILITY, KingdomAbility.LOYALTY), republic.abilityBoosts)
		assertEquals(setOf(KingdomSkill.ENGINEERING, KingdomSkill.POLITICS), republic.skillProficiencies)
		assertEquals("Pull Together", republic.bonusFeat)
	}

	@Test
	fun thaumocracyMatchesWorkbook() {
		val thaumocracy = governments.find { it.name == "Thaumocracy" }
		assertNotNull(thaumocracy)
		assertEquals(setOf(KingdomAbility.ECONOMY, KingdomAbility.CULTURE), thaumocracy.abilityBoosts)
		assertEquals(setOf(KingdomSkill.FOLKLORE, KingdomSkill.MAGIC), thaumocracy.skillProficiencies)
		assertEquals("Practical Magic", thaumocracy.bonusFeat)
	}

	@Test
	fun yeomanryMatchesWorkbook() {
		val yeomanry = governments.find { it.name == "Yeomanry" }
		assertNotNull(yeomanry)
		assertEquals(setOf(KingdomAbility.LOYALTY, KingdomAbility.CULTURE), yeomanry.abilityBoosts)
		assertEquals(setOf(KingdomSkill.AGRICULTURE, KingdomSkill.WILDERNESS), yeomanry.skillProficiencies)
		assertEquals("Muddle Through", yeomanry.bonusFeat)
	}

	@Test
	fun allGovernmentNamesAreUnique() {
		val names = governments.map { it.name }
		assertEquals(names.size, names.toSet().size, "All government names should be unique")
	}

	@Test
	fun allGovernmentsHaveTwoAbilityBoosts() {
		governments.forEach {
			assertEquals(2, it.abilityBoosts.size, "Government ${it.name} should have exactly 2 ability boosts")
		}
	}

	@Test
	fun allGovernmentsHaveTwoSkillProficiencies() {
		governments.forEach {
			assertEquals(2, it.skillProficiencies.size, "Government ${it.name} should have exactly 2 skill proficiencies")
		}
	}

	@Test
	fun allGovernmentsHaveABonusFeat() {
		governments.forEach {
			assertFalse(it.bonusFeat.isBlank(), "Government ${it.name} should have a non-blank bonus feat")
		}
	}

	@Test
	fun findGovernmentReturnsCorrectGovernment() {
		val result = findGovernment("Republic")
		assertNotNull(result)
		assertEquals("Republic", result.name)
		assertEquals("Pull Together", result.bonusFeat)
	}

	@Test
	fun findGovernmentIsCaseInsensitive() {
		val result = findGovernment("republic")
		assertNotNull(result)
		assertEquals("Republic", result.name)
	}

	@Test
	fun findGovernmentReturnsNullForInvalidName() {
		assertNull(findGovernment("Monarchy"))
	}
}
