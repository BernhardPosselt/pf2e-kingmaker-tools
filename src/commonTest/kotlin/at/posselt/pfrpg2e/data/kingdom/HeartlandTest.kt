package at.posselt.pfrpg2e.data.kingdom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HeartlandTest {

	@Test
	fun rowCountMatchesWorkbook() {
		assertEquals(4, heartlands.size, "Workbook has exactly 4 heartland terrain types")
	}

	@Test
	fun forestOrSwampMatchesWorkbook() {
		val forest = heartlands.find { it.terrain == "Forest or Swamp" }
		assertNotNull(forest)
		assertEquals(KingdomAbility.CULTURE, forest.abilityBoost)
	}

	@Test
	fun hillOrPlainMatchesWorkbook() {
		val hill = heartlands.find { it.terrain == "Hill or Plain" }
		assertNotNull(hill)
		assertEquals(KingdomAbility.LOYALTY, hill.abilityBoost)
	}

	@Test
	fun lakeOrRiverMatchesWorkbook() {
		val lake = heartlands.find { it.terrain == "Lake or River" }
		assertNotNull(lake)
		assertEquals(KingdomAbility.ECONOMY, lake.abilityBoost)
	}

	@Test
	fun mountainOrRuinsMatchesWorkbook() {
		val mountain = heartlands.find { it.terrain == "Mountain or Ruins" }
		assertNotNull(mountain)
		assertEquals(KingdomAbility.STABILITY, mountain.abilityBoost)
	}

	@Test
	fun allTerrainsAreUnique() {
		val terrains = heartlands.map { it.terrain }
		assertEquals(terrains.size, terrains.toSet().size, "All terrain names should be unique")
	}

	@Test
	fun allHeartlandsHaveDistinctAbilities() {
		val abilities = heartlands.map { it.abilityBoost }
		assertEquals(abilities.size, abilities.toSet().size, "Each heartland should boost a different ability")
	}

	@Test
	fun allFourKingdomAbilitiesAreCovered() {
		val abilities = heartlands.map { it.abilityBoost }.toSet()
		assertEquals(
			setOf(KingdomAbility.CULTURE, KingdomAbility.LOYALTY, KingdomAbility.ECONOMY, KingdomAbility.STABILITY),
			abilities,
			"All 4 kingdom abilities should be covered by heartlands"
		)
	}
}
