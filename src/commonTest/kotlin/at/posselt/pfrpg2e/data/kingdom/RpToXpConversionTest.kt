package at.posselt.pfrpg2e.data.kingdom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RpToXpRateDataTest {

    @Test
    fun tableHasEntries() {
        assertTrue(rpToXpRateData.isNotEmpty(), "RP to XP rate table should not be empty")
    }

    @Test
    fun tableHas20Entries() {
        assertEquals(20, rpToXpRateData.size, "Rate table should have 20 entries (5 level brackets x 4 size brackets)")
    }

    @Test
    fun levelBracketsExist() {
        val levelThresholds = rpToXpRateData.map { it.levelLessThan }.distinct().sorted()
        assertTrue(levelThresholds.contains(5))
        assertTrue(levelThresholds.contains(9))
        assertTrue(levelThresholds.contains(13))
        assertTrue(levelThresholds.contains(17))
        assertTrue(levelThresholds.contains(Int.MAX_VALUE))
    }

    @Test
    fun sizeBracketsExist() {
        val sizeThresholds = rpToXpRateData.map { it.sizeLessThan }.distinct().sorted()
        assertTrue(sizeThresholds.contains(10))
        assertTrue(sizeThresholds.contains(25))
        assertTrue(sizeThresholds.contains(50))
        assertTrue(sizeThresholds.contains(100))
    }

    @Test
    fun levelLessThan5RateIs100() {
        val smallKingdom = rpToXpRateData.filter { it.levelLessThan == 5 && it.sizeLessThan == 10 }
        assertEquals(1, smallKingdom.size)
        assertEquals(100, smallKingdom[0].rate, "Level < 5, size < 10 should have rate 100")
    }

    @Test
    fun level5to9RateIs10() {
        val rate = rpToXpRateData.find { it.levelLessThan == 9 && it.sizeLessThan == 10 }
        assertNotNull(rate)
        assertEquals(10, rate.rate, "Level < 9, size < 10 should have rate 10")
    }

    @Test
    fun level9to13RateVariesBySize() {
        val small = rpToXpRateData.find { it.levelLessThan == 13 && it.sizeLessThan == 10 }
        assertNotNull(small)
        assertEquals(7, small.rate)
        val large = rpToXpRateData.find { it.levelLessThan == 13 && it.sizeLessThan == 50 }
        assertNotNull(large)
        assertEquals(5, large.rate)
    }

    @Test
    fun ratesDecreaseWithLevel() {
        val size10Rates = rpToXpRateData.filter { it.sizeLessThan == 10 }.sortedBy { it.levelLessThan }
        for (i in 1 until size10Rates.size) {
            assertTrue(
                size10Rates[i].rate <= size10Rates[i - 1].rate,
                "Rate should not increase as level bracket grows"
            )
        }
    }

    @Test
    fun ratesDecreaseWithSize() {
        val level5Rates = rpToXpRateData.filter { it.levelLessThan == 5 }.sortedBy { it.sizeLessThan }
        for (i in 1 until level5Rates.size) {
            assertTrue(
                level5Rates[i].rate <= level5Rates[i - 1].rate,
                "Rate should not increase as size bracket grows"
            )
        }
    }

    @Test
    fun allRatesArePositive() {
        rpToXpRateData.forEach { rate ->
            assertTrue(rate.rate > 0, "Rate should always be positive, got ${rate.rate}")
        }
    }
}

class RandomEventXpDataTest {

    @Test
    fun tableHasEntries() {
        assertTrue(randomEventXpData.isNotEmpty(), "Random Event XP table should not be empty")
    }

    @Test
    fun tableHas5Entries() {
        assertEquals(5, randomEventXpData.size, "Random Event XP table should have 5 entries")
    }

    @Test
    fun eventLevelsCoverRange() {
        val levels = randomEventXpData.map { it.eventLevel }.sorted()
        assertEquals(-1, levels.first(), "Minimum event level modifier should be -1")
        assertEquals(3, levels.last(), "Maximum event level modifier should be 3")
    }

    @Test
    fun xpIncreasesWithEventLevel() {
        val sorted = randomEventXpData.sortedBy { it.eventLevel }
        for (i in 1 until sorted.size) {
            assertTrue(
                sorted[i].xp > sorted[i - 1].xp,
                "XP should increase with event level modifier"
            )
        }
    }

    @Test
    fun eventLevelMinus1Gives30Xp() {
        val entry = randomEventXpData.find { it.eventLevel == -1 }
        assertNotNull(entry)
        assertEquals(30, entry.xp)
    }

    @Test
    fun eventLevel0Gives40Xp() {
        val entry = randomEventXpData.find { it.eventLevel == 0 }
        assertNotNull(entry)
        assertEquals(40, entry.xp)
    }

    @Test
    fun eventLevel3Gives120Xp() {
        val entry = randomEventXpData.find { it.eventLevel == 3 }
        assertNotNull(entry)
        assertEquals(120, entry.xp)
    }

    @Test
    fun allXpValuesArePositive() {
        randomEventXpData.forEach { entry ->
            assertTrue(entry.xp > 0, "XP value should be positive")
        }
    }
}

class HexClaimedXpDataTest {

    @Test
    fun tableHasEntries() {
        assertTrue(hexClaimedXpData.isNotEmpty(), "Hex Claimed XP table should not be empty")
    }

    @Test
    fun tableHas5Entries() {
        assertEquals(5, hexClaimedXpData.size, "Hex Claimed XP table should have 5 entries")
    }

    @Test
    fun sizeBracketsExist() {
        val sizeThresholds = hexClaimedXpData.map { it.sizeLessThan }
        assertTrue(sizeThresholds.contains(10))
        assertTrue(sizeThresholds.contains(25))
        assertTrue(sizeThresholds.contains(50))
        assertTrue(sizeThresholds.contains(100))
        assertTrue(sizeThresholds.contains(Int.MAX_VALUE))
    }

    @Test
    fun xpDecreasesWithKingdomSize() {
        val sorted = hexClaimedXpData.sortedBy { it.sizeLessThan }
        for (i in 1 until sorted.size) {
            assertTrue(
                sorted[i].xp <= sorted[i - 1].xp,
                "XP should not increase with kingdom size"
            )
        }
    }

    @Test
    fun smallestKingdomGets100Xp() {
        val entry = hexClaimedXpData.find { it.sizeLessThan == 10 }
        assertNotNull(entry)
        assertEquals(100, entry.xp, "Smallest kingdom (size < 10) should get 100 XP per hex")
    }

    @Test
    fun largestKingdomGets5Xp() {
        val entry = hexClaimedXpData.find { it.sizeLessThan == Int.MAX_VALUE }
        assertNotNull(entry)
        assertEquals(5, entry.xp, "Largest kingdom should get 5 XP per hex")
    }

    @Test
    fun allXpValuesArePositive() {
        hexClaimedXpData.forEach { entry ->
            assertTrue(entry.xp > 0, "XP value should be positive")
        }
    }
}

class RpToXpRateDataClassTest {

    @Test
    fun copyPreservesFields() {
        val original = RpToXpRate(levelLessThan = 5, sizeLessThan = 10, rate = 100)
        val copied = original.copy()
        assertEquals(original.levelLessThan, copied.levelLessThan)
        assertEquals(original.sizeLessThan, copied.sizeLessThan)
        assertEquals(original.rate, copied.rate)
    }

    @Test
    fun equalsWorks() {
        val a = RpToXpRate(levelLessThan = 5, sizeLessThan = 10, rate = 100)
        val b = RpToXpRate(levelLessThan = 5, sizeLessThan = 10, rate = 100)
        assertEquals(a, b)
    }

    @Test
    fun hashCodeConsistentWithEquals() {
        val a = RpToXpRate(levelLessThan = 5, sizeLessThan = 10, rate = 100)
        val b = RpToXpRate(levelLessThan = 5, sizeLessThan = 10, rate = 100)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun toStringContainsClassName() {
        val rate = RpToXpRate(levelLessThan = 5, sizeLessThan = 10, rate = 100)
        val str = rate.toString()
        assertTrue(str.contains("RpToXpRate"))
    }
}

class RandomEventXpDataClassTest {

    @Test
    fun copyPreservesFields() {
        val original = RandomEventXp(eventLevel = 0, xp = 40)
        val copied = original.copy()
        assertEquals(original.eventLevel, copied.eventLevel)
        assertEquals(original.xp, copied.xp)
    }

    @Test
    fun equalsWorks() {
        val a = RandomEventXp(eventLevel = 0, xp = 40)
        val b = RandomEventXp(eventLevel = 0, xp = 40)
        assertEquals(a, b)
    }

    @Test
    fun hashCodeConsistentWithEquals() {
        val a = RandomEventXp(eventLevel = 0, xp = 40)
        val b = RandomEventXp(eventLevel = 0, xp = 40)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun toStringContainsClassName() {
        val entry = RandomEventXp(eventLevel = 0, xp = 40)
        val str = entry.toString()
        assertTrue(str.contains("RandomEventXp"))
    }
}

class HexClaimedXpDataClassTest {

    @Test
    fun copyPreservesFields() {
        val original = HexClaimedXp(sizeLessThan = 10, xp = 100)
        val copied = original.copy()
        assertEquals(original.sizeLessThan, copied.sizeLessThan)
        assertEquals(original.xp, copied.xp)
    }

    @Test
    fun equalsWorks() {
        val a = HexClaimedXp(sizeLessThan = 10, xp = 100)
        val b = HexClaimedXp(sizeLessThan = 10, xp = 100)
        assertEquals(a, b)
    }

    @Test
    fun hashCodeConsistentWithEquals() {
        val a = HexClaimedXp(sizeLessThan = 10, xp = 100)
        val b = HexClaimedXp(sizeLessThan = 10, xp = 100)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun toStringContainsClassName() {
        val entry = HexClaimedXp(sizeLessThan = 10, xp = 100)
        val str = entry.toString()
        assertTrue(str.contains("HexClaimedXp"))
    }
}
