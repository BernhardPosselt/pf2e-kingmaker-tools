package at.posselt.pfrpg2e.data.kingdom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KingdomFeatTest {

	@Test
	fun rowCountMatchesWorkbook() {
		assertEquals(16, kingdomFeats.size, "Workbook has exactly 16 kingdom feats")
	}

	@Test
	fun allLevel1FeatsMatchWorkbook() {
		val level1Feats = kingdomFeats.filter { it.level == 1 }
		assertEquals(9, level1Feats.size, "Workbook has 9 level-1 kingdom feats")
	}

	@Test
	fun allLevel3FeatsMatchWorkbook() {
		val level3Feats = kingdomFeats.filter { it.level == 3 }
		assertEquals(4, level3Feats.size, "Workbook has 4 level-3 kingdom feats")
	}

	@Test
	fun allLevel7FeatsMatchWorkbook() {
		val level7Feats = kingdomFeats.filter { it.level == 7 }
		assertEquals(2, level7Feats.size, "Workbook has 2 level-7 kingdom feats")
	}

	@Test
	fun level11FeatMatchesWorkbook() {
		val level11Feats = kingdomFeats.filter { it.level == 11 }
		assertEquals(1, level11Feats.size, "Workbook has 1 level-11 kingdom feat")
	}

	@Test
	fun civilServiceMatchesWorkbook() {
		val feat = kingdomFeats.find { it.name == "Civil Service" }
		assertNotNull(feat)
		assertEquals(1, feat.level)
		assertNull(feat.prerequisite, "Civil Service should have no prerequisite")
		assertTrue(feat.benefit.contains("vacancy penalty"))
	}

	@Test
	fun cooperativeLeadershipMatchesWorkbook() {
		val feat = kingdomFeats.find { it.name == "Cooperative Leadership" }
		assertNotNull(feat)
		assertEquals(1, feat.level)
		assertNull(feat.prerequisite)
		assertTrue(feat.benefit.contains("Focused Attention"))
		assertTrue(feat.benefit.contains("+3"))
	}

	@Test
	fun fortifiedFiefsMatchesWorkbook() {
		val feat = kingdomFeats.find { it.name == "Fortified Fiefs" }
		assertNotNull(feat)
		assertEquals(1, feat.level)
		assertEquals("trained in Defense", feat.prerequisite)
		assertTrue(feat.benefit.contains("Fortify Hex"))
	}

	@Test
	fun inspiringEntertainmentMatchesWorkbook() {
		val feat = kingdomFeats.find { it.name == "Inspiring Entertainment" }
		assertNotNull(feat)
		assertEquals(3, feat.level)
		assertEquals("Culture 14", feat.prerequisite)
		assertTrue(feat.benefit.contains("Culture-based check"))
	}

	@Test
	fun crushDissentMatchesWorkbook() {
		val feat = kingdomFeats.find { it.name == "Crush Dissent" }
		assertNotNull(feat)
		assertEquals(3, feat.level)
		assertEquals("trained in Warfare", feat.prerequisite)
		assertTrue(feat.benefit.contains("Unrest"))
	}

	@Test
	fun fameAndFortuneMatchesWorkbook() {
		val feat = kingdomFeats.find { it.name == "Fame and Fortune" }
		assertNotNull(feat)
		assertEquals(11, feat.level)
		assertNull(feat.prerequisite)
		assertTrue(feat.benefit.contains("critical success"))
		assertTrue(feat.benefit.contains("bonus Resource Die"))
	}

	@Test
	fun liquidateResourcesMatchesWorkbook() {
		val feat = kingdomFeats.find { it.name == "Liquidate Resources" }
		assertNotNull(feat)
		assertEquals(3, feat.level)
		assertEquals("Economy 14", feat.prerequisite)
		assertTrue(feat.benefit.contains("0 RP"))
	}

	@Test
	fun quickRecoveryMatchesWorkbook() {
		val feat = kingdomFeats.find { it.name == "Quick Recovery" }
		assertNotNull(feat)
		assertEquals(3, feat.level)
		assertEquals("Stability 14", feat.prerequisite)
		assertTrue(feat.benefit.contains("+4 status bonus"))
	}

	@Test
	fun freeAndFairMatchesWorkbook() {
		val feat = kingdomFeats.find { it.name == "Free and Fair" }
		assertNotNull(feat)
		assertEquals(7, feat.level)
		assertTrue(feat.benefit.contains("New Leadership"))
		assertTrue(feat.benefit.contains("Pledge of Fealty"))
	}

	@Test
	fun qualityOfLifeMatchesWorkbook() {
		val feat = kingdomFeats.find { it.name == "Quality of Life" }
		assertNotNull(feat)
		assertEquals(7, feat.level)
		assertTrue(feat.benefit.contains("Luxury Commodities"))
	}

	@Test
	fun allFeatNamesAreUnique() {
		val names = kingdomFeats.map { it.name }
		assertEquals(names.size, names.toSet().size, "All feat names should be unique")
	}

	@Test
	fun allFeatBenefitsAreNonBlank() {
		kingdomFeats.forEach {
			assertFalse(it.benefit.isBlank(), "Feat ${it.name} benefit should not be blank")
		}
	}

	@Test
	fun allFeatsHavePositiveLevels() {
		kingdomFeats.forEach {
			assertTrue(it.level > 0, "Feat ${it.name} level should be positive")
		}
	}

	@Test
	fun featsWithPrerequisitesMatchWorkbook() {
		val featsWithPrereqs = kingdomFeats.filter { it.prerequisite != null }
		assertEquals(10, featsWithPrereqs.size, "Workbook has 10 feats with prerequisites")
	}

	@Test
	fun findKingdomFeatReturnsCorrectFeat() {
		val result = findKingdomFeat("Skill Training")
		assertNotNull(result)
		assertEquals("Skill Training", result.name)
		assertEquals(1, result.level)
	}

	@Test
	fun findKingdomFeatIsCaseInsensitive() {
		val result = findKingdomFeat("skill training")
		assertNotNull(result)
		assertEquals("Skill Training", result.name)
	}

	@Test
	fun findKingdomFeatReturnsNullForInvalidName() {
		assertNull(findKingdomFeat("Nonexistent Feat"))
	}
}
