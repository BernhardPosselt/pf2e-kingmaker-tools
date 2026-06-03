package at.posselt.pfrpg2e.data.kingdom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class RpToXpTest {

	@Test
	fun rowCountMatchesWorkbook() {
		assertEquals(5, rpToXpTable.size, "Workbook has exactly 5 RP-to-XP rows")
	}

	@Test
	fun allRowsMatchWorkbook() {
		val expected = listOf(
			RpToXpRow(levelLessThan = 5, rate = 10, sizeLessThan = 10, xp = 100),
			RpToXpRow(levelLessThan = 9, rate = 7, sizeLessThan = 25, xp = 50),
			RpToXpRow(levelLessThan = 13, rate = 5, sizeLessThan = 50, xp = 25),
			RpToXpRow(levelLessThan = 17, rate = 2, sizeLessThan = 100, xp = 10),
			RpToXpRow(levelLessThan = 21, rate = 1, sizeLessThan = 1000, xp = 5),
		)
		assertEquals(expected, rpToXpTable)
	}

	@Test
	fun firstRowMatchesWorkbook() {
		val first = rpToXpTable[0]
		assertEquals(5, first.levelLessThan)
		assertEquals(10, first.rate)
		assertEquals(10, first.sizeLessThan)
		assertEquals(100, first.xp)
	}

	@Test
	fun lastRowMatchesWorkbook() {
		val last = rpToXpTable[4]
		assertEquals(21, last.levelLessThan)
		assertEquals(1, last.rate)
		assertEquals(1000, last.sizeLessThan)
		assertEquals(5, last.xp)
	}

	@Test
	fun secondRowMatchesWorkbook() {
		val row = rpToXpTable[1]
		assertEquals(9, row.levelLessThan)
		assertEquals(7, row.rate)
		assertEquals(25, row.sizeLessThan)
		assertEquals(50, row.xp)
	}

	@Test
	fun thirdRowMatchesWorkbook() {
		val row = rpToXpTable[2]
		assertEquals(13, row.levelLessThan)
		assertEquals(5, row.rate)
		assertEquals(50, row.sizeLessThan)
		assertEquals(25, row.xp)
	}

	@Test
	fun fourthRowMatchesWorkbook() {
		val row = rpToXpTable[3]
		assertEquals(17, row.levelLessThan)
		assertEquals(2, row.rate)
		assertEquals(100, row.sizeLessThan)
		assertEquals(10, row.xp)
	}

	@Test
	fun levelLessThanValuesAreStrictlyAscending() {
		for (i in 1 until rpToXpTable.size) {
			assertTrue(
				rpToXpTable[i].levelLessThan > rpToXpTable[i - 1].levelLessThan,
				"levelLessThan should increase: ${rpToXpTable[i - 1].levelLessThan} -> ${rpToXpTable[i].levelLessThan}",
			)
		}
	}

	@Test
	fun levelLessThanBoundaryValues() {
		val boundaries = rpToXpTable.map { it.levelLessThan }
		assertEquals(listOf(5, 9, 13, 17, 21), boundaries)
	}

	@Test
	fun rateValuesAreStrictlyDescending() {
		for (i in 1 until rpToXpTable.size) {
			assertTrue(
				rpToXpTable[i].rate < rpToXpTable[i - 1].rate,
				"rate should decrease: ${rpToXpTable[i - 1].rate} -> ${rpToXpTable[i].rate}",
			)
		}
	}

	@Test
	fun rateBoundaryValues() {
		val rates = rpToXpTable.map { it.rate }
		assertEquals(listOf(10, 7, 5, 2, 1), rates)
		assertEquals(10, rates.first(), "highest rate is 10")
		assertEquals(1, rates.last(), "lowest rate is 1")
	}

	@Test
	fun sizeLessThanValuesAreStrictlyAscending() {
		for (i in 1 until rpToXpTable.size) {
			assertTrue(
				rpToXpTable[i].sizeLessThan > rpToXpTable[i - 1].sizeLessThan,
				"sizeLessThan should increase: ${rpToXpTable[i - 1].sizeLessThan} -> ${rpToXpTable[i].sizeLessThan}",
			)
		}
	}

	@Test
	fun sizeLessThanBoundaryValues() {
		val sizes = rpToXpTable.map { it.sizeLessThan }
		assertEquals(listOf(10, 25, 50, 100, 1000), sizes)
		assertEquals(10, sizes.first(), "smallest size threshold is 10")
		assertEquals(1000, sizes.last(), "largest size threshold is 1000")
	}

	@Test
	fun xpValuesAreStrictlyDescending() {
		for (i in 1 until rpToXpTable.size) {
			assertTrue(
				rpToXpTable[i].xp < rpToXpTable[i - 1].xp,
				"XP should decrease: ${rpToXpTable[i - 1].xp} -> ${rpToXpTable[i].xp}",
			)
		}
	}

	@Test
	fun xpBoundaryValues() {
		val xps = rpToXpTable.map { it.xp }
		assertEquals(listOf(100, 50, 25, 10, 5), xps)
		assertEquals(100, xps.first(), "highest XP award is 100")
		assertEquals(5, xps.last(), "lowest XP award is 5")
	}

	@Test
	fun allValuesArePositive() {
		rpToXpTable.forEachIndexed { index, row ->
			assertTrue(row.levelLessThan > 0, "levelLessThan should be positive at row $index")
			assertTrue(row.rate > 0, "rate should be positive at row $index")
			assertTrue(row.sizeLessThan > 0, "sizeLessThan should be positive at row $index")
			assertTrue(row.xp > 0, "xp should be positive at row $index")
		}
	}

	@Test
	fun valuesRemainStableAcrossAccesses() {
		val first = rpToXpTable[0]
		val second = rpToXpTable[0]
		assertEquals(first, second, "same row on repeated access")
		assertEquals(first.levelLessThan, second.levelLessThan)
		assertEquals(first.rate, second.rate)
		assertEquals(first.sizeLessThan, second.sizeLessThan)
		assertEquals(first.xp, second.xp)
	}

	@Test
	fun allLevelLessThanBelowNine() {
		val belowNine = rpToXpTable.filter { it.levelLessThan <= 9 }
		assertEquals(2, belowNine.size)
		assertEquals(5, belowNine[0].levelLessThan)
		assertEquals(9, belowNine[1].levelLessThan)
	}

	@Test
	fun allLevelLessThanBelowSeventeen() {
		val belowSeventeen = rpToXpTable.filter { it.levelLessThan <= 17 }
		assertEquals(4, belowSeventeen.size)
		assertEquals(5, belowSeventeen[0].levelLessThan)
		assertEquals(9, belowSeventeen[1].levelLessThan)
		assertEquals(13, belowSeventeen[2].levelLessThan)
		assertEquals(17, belowSeventeen[3].levelLessThan)
	}

	@Test
	fun dataClassCopyPreservesAllFields() {
		val original = rpToXpTable[0]
		val copied = original.copy()
		assertEquals(original.levelLessThan, copied.levelLessThan)
		assertEquals(original.rate, copied.rate)
		assertEquals(original.sizeLessThan, copied.sizeLessThan)
		assertEquals(original.xp, copied.xp)
	}

	@Test
	fun dataClassCopyAllowsFieldOverride() {
		val original = rpToXpTable[0]
		val modified = original.copy(rate = 99)
		assertEquals(99, modified.rate)
		assertEquals(original.levelLessThan, modified.levelLessThan)
		assertEquals(original.sizeLessThan, modified.sizeLessThan)
		assertEquals(original.xp, modified.xp)
	}

	@Test
	fun dataClassEqualsWorks() {
		val a = RpToXpRow(levelLessThan = 5, rate = 10, sizeLessThan = 10, xp = 100)
		val b = RpToXpRow(levelLessThan = 5, rate = 10, sizeLessThan = 10, xp = 100)
		assertEquals(a, b)
	}

	@Test
	fun dataClassEqualsDetectsLevelDifference() {
		val a = RpToXpRow(levelLessThan = 5, rate = 10, sizeLessThan = 10, xp = 100)
		val b = RpToXpRow(levelLessThan = 9, rate = 10, sizeLessThan = 10, xp = 100)
		assertNotEquals(a, b)
	}

	@Test
	fun dataClassEqualsDetectsRateDifference() {
		val a = RpToXpRow(levelLessThan = 5, rate = 10, sizeLessThan = 10, xp = 100)
		val b = RpToXpRow(levelLessThan = 5, rate = 7, sizeLessThan = 10, xp = 100)
		assertNotEquals(a, b)
	}

	@Test
	fun dataClassEqualsDetectsSizeDifference() {
		val a = RpToXpRow(levelLessThan = 5, rate = 10, sizeLessThan = 10, xp = 100)
		val b = RpToXpRow(levelLessThan = 5, rate = 10, sizeLessThan = 25, xp = 100)
		assertNotEquals(a, b)
	}

	@Test
	fun dataClassEqualsDetectsXpDifference() {
		val a = RpToXpRow(levelLessThan = 5, rate = 10, sizeLessThan = 10, xp = 100)
		val b = RpToXpRow(levelLessThan = 5, rate = 10, sizeLessThan = 10, xp = 50)
		assertNotEquals(a, b)
	}

	@Test
	fun dataClassHashCodeConsistentWithEquals() {
		val a = RpToXpRow(levelLessThan = 5, rate = 10, sizeLessThan = 10, xp = 100)
		val b = RpToXpRow(levelLessThan = 5, rate = 10, sizeLessThan = 10, xp = 100)
		assertEquals(a.hashCode(), b.hashCode())
	}

	@Test
	fun dataClassToStringContainsAllFields() {
		val row = RpToXpRow(levelLessThan = 5, rate = 10, sizeLessThan = 10, xp = 100)
		val str = row.toString()
		assertTrue(str.contains("RpToXpRow"), "toString should contain class name")
		assertTrue(str.contains("5"), "toString should contain levelLessThan")
		assertTrue(str.contains("10"), "toString should contain rate/sizeLessThan/xp values")
	}

	@Test
	fun allFieldsAreIntegers() {
		rpToXpTable.forEach { row ->
			assertTrue(row.levelLessThan is Int, "levelLessThan should be Int")
			assertTrue(row.rate is Int, "rate should be Int")
			assertTrue(row.sizeLessThan is Int, "sizeLessThan should be Int")
			assertTrue(row.xp is Int, "xp should be Int")
		}
	}

	@Test
	fun noDuplicateRows() {
		assertEquals(rpToXpTable.size, rpToXpTable.toSet().size, "All rows should be unique")
	}

	@Test
	fun tableIsList() {
		assertTrue(rpToXpTable is List<RpToXpRow>, "rpToXpTable should be a List")
	}

	@Test
	fun tableRowsAreDistinctAtIndexBoundaries() {
		assertEquals(100, rpToXpTable.first().xp)
		assertEquals(5, rpToXpTable.last().xp)
		assertEquals(10, rpToXpTable.first().rate)
		assertEquals(1, rpToXpTable.last().rate)
		assertEquals(10, rpToXpTable.first().sizeLessThan)
		assertEquals(1000, rpToXpTable.last().sizeLessThan)
		assertEquals(5, rpToXpTable.first().levelLessThan)
		assertEquals(21, rpToXpTable.last().levelLessThan)
	}
}
