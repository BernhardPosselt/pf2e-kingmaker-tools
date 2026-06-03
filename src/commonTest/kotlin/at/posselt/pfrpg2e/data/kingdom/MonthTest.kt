package at.posselt.pfrpg2e.data.kingdom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MonthTest {

	@Test
	fun rowCountMatchesWorkbook() {
		assertEquals(12, months.size, "Workbook has exactly 12 months")
	}

	@Test
	fun firstMonthIsAbadius() {
		val first = months.first()
		assertEquals("Abadius", first.name)
		assertEquals(1, first.order)
	}

	@Test
	fun lastMonthIsKuthona() {
		val last = months.last()
		assertEquals("Kuthona", last.name)
		assertEquals(12, last.order)
	}

	@Test
	fun allMonthNamesMatchWorkbook() {
		val expectedNames = listOf(
			"Abadius", "Calistril", "Pharast", "Gozran",
			"Desnus", "Sarenith", "Erastus", "Arodus",
			"Rova", "Lamashan", "Neth", "Kuthona",
		)
		assertEquals(expectedNames, months.map { it.name })
	}

	@Test
	fun allOrdersAreSequential() {
		months.forEachIndexed { index, month ->
			assertEquals(index + 1, month.order, "Month at index $index should have order ${index + 1}")
		}
	}

	@Test
	fun allMonthNamesAreUnique() {
		val names = months.map { it.name }
		assertEquals(names.size, names.toSet().size, "All month names should be unique")
	}

	@Test
	fun allOrdersAreUnique() {
		val orders = months.map { it.order }
		assertEquals(orders.size, orders.toSet().size, "All month orders should be unique")
	}

	@Test
	fun monthsCoverFullRange() {
		val expected = (1..12).toList()
		assertEquals(expected, months.map { it.order })
	}
}

class ProficiencyBonusTest {

	@Test
	fun rowCountMatchesWorkbook() {
		assertEquals(5, proficiencyBonuses.size, "Workbook has exactly 5 proficiency ranks")
	}

	@Test
	fun untrainedBonusIsZero() {
		val untrained = proficiencyBonuses.find { it.rank == "Untrained" }
		assertNotNull(untrained)
		assertEquals(0, untrained.bonus)
	}

	@Test
	fun trainedBonusIsTwo() {
		val trained = proficiencyBonuses.find { it.rank == "Trained" }
		assertNotNull(trained)
		assertEquals(2, trained.bonus)
	}

	@Test
	fun expertBonusIsFour() {
		val expert = proficiencyBonuses.find { it.rank == "Expert" }
		assertNotNull(expert)
		assertEquals(4, expert.bonus)
	}

	@Test
	fun masterBonusIsSix() {
		val master = proficiencyBonuses.find { it.rank == "Master" }
		assertNotNull(master)
		assertEquals(6, master.bonus)
	}

	@Test
	fun legendaryBonusIsEight() {
		val legendary = proficiencyBonuses.find { it.rank == "Legendary" }
		assertNotNull(legendary)
		assertEquals(8, legendary.bonus)
	}

	@Test
	fun allRanksMatchWorkbook() {
		val expectedRanks = listOf("Untrained", "Trained", "Expert", "Master", "Legendary")
		assertEquals(expectedRanks, proficiencyBonuses.map { it.rank })
	}

	@Test
	fun bonusesIncreaseWithRank() {
		for (i in 1 until proficiencyBonuses.size) {
			assertTrue(
				proficiencyBonuses[i].bonus > proficiencyBonuses[i - 1].bonus,
				"Bonus should increase: ${proficiencyBonuses[i - 1].rank} -> ${proficiencyBonuses[i].rank}",
			)
		}
	}

	@Test
	fun allRanksAreUnique() {
		val ranks = proficiencyBonuses.map { it.rank }
		assertEquals(ranks.size, ranks.toSet().size, "All rank names should be unique")
	}
}
