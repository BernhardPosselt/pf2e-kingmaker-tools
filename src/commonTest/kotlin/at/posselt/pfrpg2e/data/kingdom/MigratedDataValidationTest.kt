package at.posselt.pfrpg2e.data.kingdom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Additional validation tests for data migrated from the Kingdom Sheet workbook.
 * Tests cover migrated field values, derived values, and cross-reference integrity
 * that are not covered by the per-class test files.
 */

class KingdomFeatMigrationTest {

    @Test
    fun insiderTradingMatchesWorkbook() {
        val feat = kingdomFeats.find { it.name == "Insider Trading" }
        assertNotNull(feat)
        assertEquals(1, feat.level)
        assertEquals("trained in Industry", feat.prerequisite)
        assertTrue(feat.benefit.contains("Establish Work Site"))
        assertTrue(feat.benefit.contains("Establish Trade Agreement"))
        assertTrue(feat.benefit.contains("Trade Commodities"))
    }

    @Test
    fun kingdomAssuranceMatchesWorkbook() {
        val feat = kingdomFeats.find { it.name == "Kingdom Assurance" }
        assertNotNull(feat)
        assertEquals(1, feat.level)
        assertEquals("trained in at least three skills", feat.prerequisite)
        assertTrue(feat.benefit.contains("10 + your proficiency bonus"))
    }

    @Test
    fun muddleThroughMatchesWorkbook() {
        val feat = kingdomFeats.find { it.name == "Muddle Through" }
        assertNotNull(feat)
        assertEquals(1, feat.level)
        assertEquals("trained in Wilderness", feat.prerequisite)
        assertTrue(feat.benefit.contains("Ruin thresholds"))
    }

    @Test
    fun pullTogetherMatchesWorkbook() {
        val feat = kingdomFeats.find { it.name == "Pull Together" }
        assertNotNull(feat)
        assertEquals(1, feat.level)
        assertEquals("trained in Politics", feat.prerequisite)
        assertTrue(feat.benefit.contains("critical failure"))
        assertTrue(feat.benefit.contains("DC 11 flat check"))
    }

    @Test
    fun skillTrainingMatchesWorkbook() {
        val feat = kingdomFeats.find { it.name == "Skill Training" }
        assertNotNull(feat)
        assertEquals(1, feat.level)
        assertNull(feat.prerequisite)
        assertTrue(feat.benefit.contains("trained proficiency rank"))
    }

    @Test
    fun allLevel1FeatNamesMatchWorkbook() {
        val level1Names = kingdomFeats.filter { it.level == 1 }.map { it.name }.sorted()
        val expected = listOf(
            "Civil Service", "Cooperative Leadership", "Fortified Fiefs",
            "Insider Trading", "Kingdom Assurance", "Muddle Through",
            "Practical Magic", "Pull Together", "Skill Training"
        ).sorted()
        assertEquals(expected, level1Names)
    }

    @Test
    fun allLevel3FeatNamesMatchWorkbook() {
        val level3Names = kingdomFeats.filter { it.level == 3 }.map { it.name }.sorted()
        val expected = listOf("Crush Dissent", "Inspiring Entertainment", "Liquidate Resources", "Quick Recovery").sorted()
        assertEquals(expected, level3Names)
    }

    @Test
    fun allLevel7FeatNamesMatchWorkbook() {
        val level7Names = kingdomFeats.filter { it.level == 7 }.map { it.name }.sorted()
        val expected = listOf("Free and Fair", "Quality of Life").sorted()
        assertEquals(expected, level7Names)
    }

    @Test
    fun level11FeatNameMatchesWorkbook() {
        val level11Names = kingdomFeats.filter { it.level == 11 }.map { it.name }
        assertEquals(listOf("Fame and Fortune"), level11Names)
    }

    @Test
    fun featsWithoutPrerequisitesMatchWorkbook() {
        val noPrereqs = kingdomFeats.filter { it.prerequisite == null }.map { it.name }.sorted()
        val expected = listOf(
            "Civil Service", "Cooperative Leadership", "Fame and Fortune",
            "Free and Fair", "Quality of Life", "Skill Training"
        ).sorted()
        assertEquals(expected, noPrereqs)
    }

    @Test
    fun featsWithStatPrerequisitesReferenceValidAbilities() {
        val statPrereqs = kingdomFeats.mapNotNull { it.prerequisite?.let { p -> it.name to p } }
            .filter { (_, prereq) -> prereq.contains("14") }
        statPrereqs.forEach { (featName, prereq) ->
            assertTrue(
                prereq == "Culture 14" || prereq == "Economy 14" || prereq == "Stability 14",
                "Feat $featName has unexpected stat prerequisite: $prereq"
            )
        }
    }
}

class ArmyTacticMigrationTest {

    @Test
    fun tableHasExpectedRowCount() {
        // Total tactics: holdtheline, ambush, bloodied, cavalryexperts, darkvision,
        // defensivetactics, explosiveshot, fieldtriage, flaming, flexible, focuseddevotion,
        // increasedammunition, keenedyed, keepupthepressure, liveofftheland, lowlightvision,
        // merciless, opening salvo, recklessflankers, sharpshooter, toughened, overrun,
        // enginesofwar, burningweaponry, explosivedefeat, swampdwellers, amphibious,
        // chorusofcroaks, swampcharge, brave, selfsufficient, trample, accustomedtopanic,
        // hurlnets, waterretreat, waterstride, furiouscharge, reactiverally, revelinbattle, warmongers
        assertEquals(39, armyTacticData.size, "Should have 39 army tactics from workbook")
    }

    @Test
    fun bloodiedButUnbrokenCoversCavalryInfantrySkirmisher() {
        val tactic = armyTacticData.find { it.name == "Bloodied, but Unbroken" }
        assertNotNull(tactic)
        assertEquals(5, tactic.minimumLevel)
        assertTrue(tactic.armyTypes.containsAll(listOf(ArmyType.CAVALRY, ArmyType.INFANTRY, ArmyType.SKIRMISHER)))
        assertFalse(tactic.armyTypes.contains(ArmyType.SIEGE))
        assertTrue(tactic.description.contains("Rout Threshold"))
    }

    @Test
    fun cavalryExpertsIsCavalryOnly() {
        val tactic = armyTacticData.find { it.name == "Cavalry Experts" }
        assertNotNull(tactic)
        assertEquals(6, tactic.minimumLevel)
        assertEquals(listOf(ArmyType.CAVALRY), tactic.armyTypes)
        assertTrue(tactic.description.contains("Overrun"))
    }

    @Test
    fun defensiveTacticsTraitAndAction() {
        val tactic = armyTacticData.find { it.name == "Defensive Tactics" }
        assertNotNull(tactic)
        assertEquals(3, tactic.minimumLevel)
        assertEquals("Maneuver", tactic.trait)
        assertTrue(tactic.grantedActions.any { it.contains("Defensive Stance") })
    }

    @Test
    fun explosiveShotIsSiegeOnly() {
        val tactic = armyTacticData.find { it.name == "Explosive Shot" }
        assertNotNull(tactic)
        assertEquals(11, tactic.minimumLevel)
        assertEquals(listOf(ArmyType.SIEGE), tactic.armyTypes)
        assertTrue(tactic.grantedActions.any { it.contains("Overwhelming Bombardment") })
    }

    @Test
    fun fieldTriageCoversInfantrySkirmisher() {
        val tactic = armyTacticData.find { it.name == "Field Triage" }
        assertNotNull(tactic)
        assertEquals(6, tactic.minimumLevel)
        assertTrue(tactic.armyTypes.containsAll(listOf(ArmyType.INFANTRY, ArmyType.SKIRMISHER)))
        assertFalse(tactic.armyTypes.contains(ArmyType.CAVALRY))
        assertTrue(tactic.grantedActions.any { it.contains("Battlefield Medicine") })
    }

    @Test
    fun flamingShotAvailableAtLevel9() {
        val tactic = armyTacticData.find { it.name == "Flaming Shot" }
        assertNotNull(tactic)
        assertEquals(9, tactic.minimumLevel)
        assertTrue(tactic.description.contains("ignite"))
    }

    @Test
    fun flexibleTacticsFourGrantedActions() {
        val tactic = armyTacticData.find { it.name == "Flexible Tactics" }
        assertNotNull(tactic)
        assertEquals(5, tactic.minimumLevel)
        assertTrue(tactic.grantedActions.any { it.contains("Dirty Fighting") })
        assertTrue(tactic.grantedActions.any { it.contains("False Retreat") })
        assertTrue(tactic.grantedActions.any { it.contains("Feint") })
        assertTrue(tactic.grantedActions.any { it.contains("Counterattack") })
    }

    @Test
    fun focusedDevotionGrantsTaunt() {
        val tactic = armyTacticData.find { it.name == "Focused Devotion" }
        assertNotNull(tactic)
        assertEquals(3, tactic.minimumLevel)
        assertEquals("Morale", tactic.trait)
        assertTrue(tactic.grantedActions.any { it.contains("Taunt") })
    }

    @Test
    fun mercilessGrantsAllOutAssault() {
        val tactic = armyTacticData.find { it.name == "Merciless" }
        assertNotNull(tactic)
        assertEquals(5, tactic.minimumLevel)
        assertTrue(tactic.armyTypes.containsAll(listOf(ArmyType.CAVALRY, ArmyType.INFANTRY)))
        assertFalse(tactic.armyTypes.contains(ArmyType.SKIRMISHER))
        assertTrue(tactic.grantedActions.any { it.contains("All-Out Assault") })
    }

    @Test
    fun openingSalvoLevel5() {
        val tactic = armyTacticData.find { it.name == "Opening Salvo" }
        assertNotNull(tactic)
        assertEquals(5, tactic.minimumLevel)
        assertTrue(tactic.armyTypes.containsAll(listOf(ArmyType.CAVALRY, ArmyType.SIEGE, ArmyType.SKIRMISHER)))
    }

    @Test
    fun recklessFlankersGrantsOutflank() {
        val tactic = armyTacticData.find { it.name == "Reckless Flankers" }
        assertNotNull(tactic)
        assertEquals(5, tactic.minimumLevel)
        assertEquals("Maneuver", tactic.trait)
        assertTrue(tactic.grantedActions.any { it.contains("Outflank") })
    }

    @Test
    fun sharpshooterGrantsCoveringFire() {
        val tactic = armyTacticData.find { it.name == "Sharpshooter" }
        assertNotNull(tactic)
        assertEquals(5, tactic.minimumLevel)
        assertEquals("Attack", tactic.trait)
        assertTrue(tactic.grantedActions.any { it.contains("Covering Fire") })
    }

    @Test
    fun toughenedSoldiersIsRepeatable() {
        val tactic = armyTacticData.find { it.name == "Toughened Soldiers" }
        assertNotNull(tactic)
        assertEquals(1, tactic.minimumLevel)
        assertTrue(tactic.description.contains("multiple times"))
        assertTrue(tactic.description.contains("maximum Hit Points"))
    }

    @Test
    fun keenEyedLevel1() {
        val tactic = armyTacticData.find { it.name == "Keen Eyed" }
        assertNotNull(tactic)
        assertEquals(1, tactic.minimumLevel)
        assertTrue(tactic.description.contains("initiative"))
    }

    @Test
    fun keepUpThePressureLevel3() {
        val tactic = armyTacticData.find { it.name == "Keep Up the Pressure" }
        assertNotNull(tactic)
        assertEquals(3, tactic.minimumLevel)
        assertTrue(tactic.description.contains("multiple attack penalty"))
    }

    @Test
    fun liveOffTheLandLevel1() {
        val tactic = armyTacticData.find { it.name == "Live off the Land" }
        assertNotNull(tactic)
        assertEquals(1, tactic.minimumLevel)
        assertFalse(tactic.armyTypes.contains(ArmyType.SIEGE))
        assertTrue(tactic.description.contains("Consumption"))
    }

    @Test
    fun lowLightVisionLevel1() {
        val tactic = armyTacticData.find { it.name == "Low-Light Vision" }
        assertNotNull(tactic)
        assertEquals(1, tactic.minimumLevel)
        assertTrue(tactic.description.contains("low-light vision"))
    }

    // Unique tactic spot-checks

    @Test
    fun burningWeaponryIsUniqueLevel7() {
        val tactic = armyTacticData.find { it.name == "Burning Weaponry" }
        assertNotNull(tactic)
        assertEquals(7, tactic.minimumLevel)
        assertEquals(listOf(ArmyType.UNIQUE), tactic.armyTypes)
        assertTrue(tactic.description.contains("Greengripe"))
    }

    @Test
    fun explosiveDefeatIsUniqueLevel7() {
        val tactic = armyTacticData.find { it.name == "Explosive Defeat" }
        assertNotNull(tactic)
        assertEquals(7, tactic.minimumLevel)
        assertEquals(listOf(ArmyType.UNIQUE), tactic.armyTypes)
        assertTrue(tactic.description.contains("explodes"))
    }

    @Test
    fun swampDwellersIsUniqueLevel5() {
        val tactic = armyTacticData.find { it.name == "Swamp Dwellers" }
        assertNotNull(tactic)
        assertEquals(5, tactic.minimumLevel)
        assertEquals(listOf(ArmyType.UNIQUE), tactic.armyTypes)
        assertTrue(tactic.description.contains("Live off the Land"))
    }

    @Test
    fun amphibiousIsUniqueLevel10() {
        val tactic = armyTacticData.find { it.name == "Amphibious" }
        assertNotNull(tactic)
        assertEquals(10, tactic.minimumLevel)
        assertEquals(listOf(ArmyType.UNIQUE), tactic.armyTypes)
        assertTrue(tactic.description.contains("Frog Riders"))
    }

    @Test
    fun chorusOfCroaksIsUniqueLevel10() {
        val tactic = armyTacticData.find { it.name == "Chorus of Croaks" }
        assertNotNull(tactic)
        assertEquals(10, tactic.minimumLevel)
        assertEquals(listOf(ArmyType.UNIQUE), tactic.armyTypes)
        assertTrue(tactic.description.contains("croaking"))
    }

    @Test
    fun swampChargeIsUniqueLevel10() {
        val tactic = armyTacticData.find { it.name == "Swamp Charge" }
        assertNotNull(tactic)
        assertEquals(10, tactic.minimumLevel)
        assertEquals(listOf(ArmyType.UNIQUE), tactic.armyTypes)
        assertTrue(tactic.description.contains("Frog Riders"))
    }

    @Test
    fun braveIsUniqueLevel8() {
        val tactic = armyTacticData.find { it.name == "Brave" }
        assertNotNull(tactic)
        assertEquals(8, tactic.minimumLevel)
        assertEquals(listOf(ArmyType.UNIQUE), tactic.armyTypes)
        assertTrue(tactic.description.contains("Nomen"))
    }

    @Test
    fun selfSufficientIsUniqueLevel8() {
        val tactic = armyTacticData.find { it.name == "Self-Sufficient" }
        assertNotNull(tactic)
        assertEquals(8, tactic.minimumLevel)
        assertEquals(listOf(ArmyType.UNIQUE), tactic.armyTypes)
        assertTrue(tactic.description.contains("consumption"))
    }

    @Test
    fun trampleIsUniqueLevel8() {
        val tactic = armyTacticData.find { it.name == "Trample" }
        assertNotNull(tactic)
        assertEquals(8, tactic.minimumLevel)
        assertEquals(listOf(ArmyType.UNIQUE), tactic.armyTypes)
        assertTrue(tactic.description.contains("trample"))
    }

    @Test
    fun accustomedToPanicIsUniqueLevel3() {
        val tactic = armyTacticData.find { it.name == "Accustomed to Panic" }
        assertNotNull(tactic)
        assertEquals(3, tactic.minimumLevel)
        assertEquals(listOf(ArmyType.UNIQUE), tactic.armyTypes)
        assertEquals("Morale", tactic.trait)
        assertTrue(tactic.grantedActions.any { it.contains("Perseverance") })
    }

    @Test
    fun hurlNetsIsUniqueLevel10() {
        val tactic = armyTacticData.find { it.name == "Hurl Nets" }
        assertNotNull(tactic)
        assertEquals(10, tactic.minimumLevel)
        assertEquals(listOf(ArmyType.UNIQUE), tactic.armyTypes)
        assertTrue(tactic.description.contains("mired"))
    }

    @Test
    fun waterRetreatIsUniqueLevel10() {
        val tactic = armyTacticData.find { it.name == "Water Retreat" }
        assertNotNull(tactic)
        assertEquals(10, tactic.minimumLevel)
        assertEquals(listOf(ArmyType.UNIQUE), tactic.armyTypes)
        assertTrue(tactic.description.contains("Disengage"))
    }

    @Test
    fun waterStrideIsUniqueLevel10() {
        val tactic = armyTacticData.find { it.name == "Water Stride" }
        assertNotNull(tactic)
        assertEquals(10, tactic.minimumLevel)
        assertEquals(listOf(ArmyType.UNIQUE), tactic.armyTypes)
        assertTrue(tactic.description.contains("watery surfaces"))
    }

    @Test
    fun furiousChargeIsUniqueLevel10() {
        val tactic = armyTacticData.find { it.name == "Furious Charge" }
        assertNotNull(tactic)
        assertEquals(10, tactic.minimumLevel)
        assertEquals(listOf(ArmyType.UNIQUE), tactic.armyTypes)
        assertTrue(tactic.grantedActions.any { it.contains("Furious Charge") })
    }

    @Test
    fun reactiveRallyIsUniqueLevel10() {
        val tactic = armyTacticData.find { it.name == "Reactive Rally" }
        assertNotNull(tactic)
        assertEquals(10, tactic.minimumLevel)
        assertEquals(listOf(ArmyType.UNIQUE), tactic.armyTypes)
        assertTrue(tactic.grantedActions.any { it.contains("Reactive Rally") })
    }

    @Test
    fun revelInBattleIsUniqueLevel10() {
        val tactic = armyTacticData.find { it.name == "Revel in Battle" }
        assertNotNull(tactic)
        assertEquals(10, tactic.minimumLevel)
        assertEquals(listOf(ArmyType.UNIQUE), tactic.armyTypes)
        assertTrue(tactic.grantedActions.any { it.contains("Revel in Battle") })
    }

    @Test
    fun warmongersIsUniqueLevel10() {
        val tactic = armyTacticData.find { it.name == "Warmongers" }
        assertNotNull(tactic)
        assertEquals(10, tactic.minimumLevel)
        assertEquals(listOf(ArmyType.UNIQUE), tactic.armyTypes)
        assertTrue(tactic.grantedActions.any { it.contains("All-Out Assault") })
        assertTrue(tactic.grantedActions.any { it.contains("Counterattack") })
        assertTrue(tactic.grantedActions.any { it.contains("Taunt") })
    }

    @Test
    fun allTacticNamesAreUnique() {
        val names = armyTacticData.map { it.name.lowercase() }
        assertEquals(names.size, names.toSet().size, "All tactic names should be unique")
    }

    @Test
    fun basicTacticsExcludeUniqueType() {
        val basicTypes = listOf(ArmyType.CAVALRY, ArmyType.INFANTRY, ArmyType.SIEGE, ArmyType.SKIRMISHER)
        val basicTactics = armyTacticData.filter { tactic ->
            tactic.armyTypes.any { it in basicTypes } && !tactic.armyTypes.contains(ArmyType.UNIQUE)
        }
        assertTrue(basicTactics.isNotEmpty(), "Should have tactics for basic army types only")
    }

    @Test
    fun uniqueTacticsOnlyApplyToUniqueType() {
        val uniqueTactics = armyTacticData.filter { it.armyTypes.contains(ArmyType.UNIQUE) }
        uniqueTactics.forEach { tactic ->
            assertEquals(
                listOf(ArmyType.UNIQUE), tactic.armyTypes,
                "Tactic '${tactic.name}' applies to UNIQUE but also to other types: ${tactic.armyTypes}"
            )
        }
    }
}

class ArmyStatsIntermediateSpotCheckTest {

    @Test
    fun level5StatsMatchWorkbook() {
        val stats = findArmyStats(5)
        assertNotNull(stats)
        assertEquals(5, stats.level)
        assertEquals(12, stats.scouting)
        assertEquals(20, stats.standardDc)
        assertEquals(22, stats.ac)
        assertEquals(15, stats.highSave)
        assertEquals(9, stats.lowSave)
        assertEquals(15, stats.attack)
        assertEquals(2, stats.maxTactics)
    }

    @Test
    fun level10StatsMatchWorkbook() {
        val stats = findArmyStats(10)
        assertNotNull(stats)
        assertEquals(10, stats.level)
        assertEquals(19, stats.scouting)
        assertEquals(27, stats.standardDc)
        assertEquals(30, stats.ac)
        assertEquals(22, stats.highSave)
        assertEquals(16, stats.lowSave)
        assertEquals(23, stats.attack)
        assertEquals(3, stats.maxTactics)
    }

    @Test
    fun level15StatsMatchWorkbook() {
        val stats = findArmyStats(15)
        assertNotNull(stats)
        assertEquals(15, stats.level)
        assertEquals(26, stats.scouting)
        assertEquals(34, stats.standardDc)
        assertEquals(37, stats.ac)
        assertEquals(29, stats.highSave)
        assertEquals(24, stats.lowSave)
        assertEquals(30, stats.attack)
        assertEquals(4, stats.maxTactics)
    }

    @Test
    fun attackIncreasesWithLevel() {
        for (i in 1 until armyStatsData.size) {
            assertTrue(
                armyStatsData[i].attack > armyStatsData[i - 1].attack,
                "Attack should strictly increase from level ${i} to ${i + 1}"
            )
        }
    }

    @Test
    fun highSaveAlwaysGreaterThanLowSave() {
        armyStatsData.forEach { stats ->
            assertTrue(
                stats.highSave > stats.lowSave,
                "Level ${stats.level}: highSave (${stats.highSave}) should be > lowSave (${stats.lowSave})"
            )
        }
    }

    @Test
    fun standardDcIncreasesWithLevel() {
        for (i in 1 until armyStatsData.size) {
            assertTrue(
                armyStatsData[i].standardDc > armyStatsData[i - 1].standardDc,
                "Standard DC should strictly increase from level ${i} to ${i + 1}"
            )
        }
    }
}

class CrossReferenceIntegrityTest {

    @Test
    fun governmentBonusFeatNamesMatchKingdomFeatNames() {
        val featNames = kingdomFeats.map { it.name }.toSet()
        governments.forEach { government ->
            assertTrue(
                government.bonusFeat in featNames,
                "Government '${government.name}' bonus feat '${government.bonusFeat}' does not match any KingdomFeat"
            )
        }
    }

    @Test
    fun armyTemplateStartingTacticNamesMatchTacticNames() {
        val tacticNames = armyTacticData.map { it.name }.toSet()
        armyTemplateData.forEach { template ->
            template.startingTactics.forEach { tacticName ->
                assertTrue(
                    tacticName in tacticNames,
                    "Army template '${template.name}' starting tactic '$tacticName' does not match any ArmyTactic"
                )
            }
        }
    }

    @Test
    fun specializedArmyModifierNamesMatchTemplateNames() {
        val templateNames = armyTemplateData.map { it.name }.toSet()
        specializedArmyModifierData.forEach { modifier ->
            assertTrue(
                modifier.armyName in templateNames,
                "SpecializedArmyModifier '${modifier.armyName}' does not match any ArmyTemplate"
            )
        }
    }

    @Test
    fun charterFlawBoostReferenceValidAbilities() {
        val validAbilities = KingdomAbility.entries.toSet()
        charters.forEach { charter ->
            charter.boost?.let { assertTrue(it in validAbilities) }
            charter.flaw?.let { assertTrue(it in validAbilities) }
        }
    }

    @Test
    fun governmentSkillProficienciesReferenceValidSkills() {
        val validSkills = KingdomSkill.entries.toSet()
        governments.forEach { government ->
            government.skillProficiencies.forEach { skill ->
                assertTrue(
                    skill in validSkills,
                    "Government '${government.name}' skill $skill is not a valid KingdomSkill"
                )
            }
        }
    }

    @Test
    fun heartlandAbilitiesAreAllFourKingdomAbilities() {
        val heartlandAbilities = heartlands.map { it.abilityBoost }.toSet()
        assertEquals(
            setOf(KingdomAbility.CULTURE, KingdomAbility.LOYALTY, KingdomAbility.ECONOMY, KingdomAbility.STABILITY),
            heartlandAbilities,
            "Heartlands should cover all 4 kingdom abilities"
        )
    }
}

class ArmyTemplateSpotCheckTest {

    @Test
    fun greengribeBombardiersStatsMatchWorkbook() {
        val template = armyTemplateData.find { it.name == "Greengripe Bombardiers" }
        assertNotNull(template)
        assertEquals(ArmyType.UNIQUE, template.type)
        assertFalse(template.accessible)
        assertEquals(2, template.consumption)
        assertEquals(6, template.hp)
        assertEquals(7, template.minimumLevel)
        assertEquals(ArmyAttackType.BOTH, template.attackType)
        assertEquals(5, template.rangedAmmo)
        assertEquals(ArmySaveBonus.LOW, template.maneuverSave)
        assertEquals("Greengripe", template.specialFaction)
        assertEquals(3, template.startingTactics.size)
        assertTrue(template.startingTactics.contains("Burning Weaponry"))
        assertTrue(template.startingTactics.contains("Darkvision"))
        assertTrue(template.startingTactics.contains("Explosive Defeat"))
    }

    @Test
    fun tokNikratScoutsStatsMatchWorkbook() {
        val template = armyTemplateData.find { it.name == "Tok-Nikrat Scouts" }
        assertNotNull(template)
        assertEquals(ArmyType.UNIQUE, template.type)
        assertFalse(template.accessible)
        assertEquals(1, template.consumption)
        assertEquals(4, template.hp)
        assertEquals(10, template.minimumLevel)
        assertEquals(ArmyAttackType.BOTH, template.attackType)
        assertEquals("Tok-Nikrat", template.specialFaction)
        assertTrue(template.startingTactics.contains("Hurl Nets"))
        assertTrue(template.startingTactics.contains("Water Retreat"))
        assertTrue(template.startingTactics.contains("Water Stride"))
    }

    @Test
    fun lizardfolkDefendersStatsMatchWorkbook() {
        val template = armyTemplateData.find { it.name == "Lizardfolk Defenders" }
        assertNotNull(template)
        assertEquals(ArmyType.UNIQUE, template.type)
        assertFalse(template.accessible)
        assertEquals(5, template.minimumLevel)
        assertEquals(ArmyAttackType.BOTH, template.attackType)
        assertEquals("Candlemere Lizardfolk", template.specialFaction)
        assertTrue(template.startingTactics.contains("Swamp Dwellers"))
    }

    @Test
    fun skirmishersBasicAccessible() {
        val template = armyTemplateData.find { it.name == "Skirmishers" }
        assertNotNull(template)
        assertEquals(ArmyType.SKIRMISHER, template.type)
        assertTrue(template.accessible)
        assertEquals(1, template.consumption)
        assertEquals(4, template.hp)
        assertEquals(5, template.minimumLevel)
        assertEquals(ArmyAttackType.MELEE, template.attackType)
        assertEquals(ArmySaveBonus.HIGH, template.maneuverSave)
    }

    @Test
    fun totalArmyTemplateCountIsTen() {
        assertEquals(10, armyTemplateData.size, "Should have 10 army templates (4 basic + 6 unique)")
    }

    @Test
    fun sixUniqueArmiesExist() {
        val unique = armyTemplateData.filter { it.type == ArmyType.UNIQUE }
        assertEquals(6, unique.size, "Should have 6 unique army templates")
    }
}

class RpToXpDerivedValueTest {

    @Test
    fun rateDescendsAsLevelIncreases() {
        // Higher level = lower RP-to-XP rate (it costs more RP to earn XP at high levels)
        for (i in 1 until rpToXpTable.size) {
            assertTrue(
                rpToXpTable[i].rate < rpToXpTable[i - 1].rate,
                "RP-to-XP rate should strictly decrease at each tier"
            )
        }
    }

    @Test
    fun xpDescendsAsKingdomSizeIncreases() {
        // Larger kingdoms get less XP per tier
        for (i in 1 until rpToXpTable.size) {
            assertTrue(
                rpToXpTable[i].xp < rpToXpTable[i - 1].xp,
                "XP award should strictly decrease at each tier"
            )
        }
    }

    @Test
    fun levelThresholdsAlignWithLevelBands() {
        assertEquals(listOf(5, 9, 13, 17, 21), rpToXpTable.map { it.levelLessThan })
    }

    @Test
    fun sizeThresholdsAlignWithSizeBands() {
        assertEquals(listOf(10, 25, 50, 100, 1000), rpToXpTable.map { it.sizeLessThan })
    }
}
