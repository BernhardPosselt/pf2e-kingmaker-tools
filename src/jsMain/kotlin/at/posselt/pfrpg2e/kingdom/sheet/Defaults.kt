package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill.*
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.KingdomSettings
import at.posselt.pfrpg2e.kingdom.RawLeaderKingdomSkills
import at.posselt.pfrpg2e.kingdom.RawLeaderSkills
import at.posselt.pfrpg2e.kingdom.data.MilestoneChoice
import at.posselt.pfrpg2e.kingdom.data.RawAbilityBoostChoices
import at.posselt.pfrpg2e.kingdom.data.RawAbilityScores
import at.posselt.pfrpg2e.kingdom.data.RawCharterChoices
import at.posselt.pfrpg2e.kingdom.data.RawCommodities
import at.posselt.pfrpg2e.kingdom.data.RawConsumption
import at.posselt.pfrpg2e.kingdom.data.RawCurrentCommodities
import at.posselt.pfrpg2e.kingdom.data.RawFame
import at.posselt.pfrpg2e.kingdom.data.RawGovernmentChoices
import at.posselt.pfrpg2e.kingdom.data.RawHeartlandChoices
import at.posselt.pfrpg2e.kingdom.data.RawLeaderValues
import at.posselt.pfrpg2e.kingdom.data.RawLeaders
import at.posselt.pfrpg2e.kingdom.data.RawNotes
import at.posselt.pfrpg2e.kingdom.data.RawResources
import at.posselt.pfrpg2e.kingdom.data.RawRuin
import at.posselt.pfrpg2e.kingdom.data.RawRuinValues
import at.posselt.pfrpg2e.kingdom.data.RawSkillRanks
import at.posselt.pfrpg2e.kingdom.data.RawWorkSite
import at.posselt.pfrpg2e.kingdom.data.RawWorkSites
import at.posselt.pfrpg2e.kingdom.kingdomActivities
import at.posselt.pfrpg2e.kingdom.kingdomMilestones

fun createKingdomDefaults() =
    KingdomData(
        name = "Kingdom",
        atWar = false,
        fame = RawFame(type = "famous", now = 0, next = 0),
        level = 1,
        xpThreshold = 1000,
        xp = 0,
        size = 1,
        unrest = 0,
        resourcePoints = RawResources(now = 0, next = 0),
        resourceDice = RawResources(now = 0, next = 0),
        workSites = RawWorkSites(
            farmlands = RawWorkSite(
                quantity = 0,
                resources = 0,
            ),
            lumberCamps = RawWorkSite(
                quantity = 0,
                resources = 0,
            ),
            mines = RawWorkSite(
                quantity = 0,
                resources = 0,
            ),
            quarries = RawWorkSite(
                quantity = 0,
                resources = 0,
            ),
            luxurySources = RawWorkSite(
                quantity = 0,
                resources = 0,
            ),
        ),
        consumption = RawConsumption(
            armies = 0,
            now = 0,
            next = 0,
        ),
        supernaturalSolutions = 0,
        creativeSolutions = 0,
        settings = KingdomSettings(
            rpToXpConversionRate = 1,
            rpToXpConversionLimit = 120,
            automateStats = true,
            resourceDicePerVillage = 0,
            resourceDicePerTown = 0,
            ruinThreshold = 10,
            increaseScorePicksBy = 0,
            resourceDicePerCity = 0,
            resourceDicePerMetropolis = 0,
            realmSceneId = null,
            xpPerClaimedHex = 10,
            includeCapitalItemModifier = true,
            cultOfTheBloomEvents = false,
            autoCalculateSettlementLevel = true,
            vanceAndKerensharaXP = false,
            capitalInvestmentInCapital = false,
            reduceDCToBuildLumberStructures = false,
            kingdomSkillIncreaseEveryLevel = false,
            kingdomAllStructureItemBonusesStack = false,
            kingdomIgnoreSkillRequirements = false,
            autoCalculateArmyConsumption = true,
            enableLeadershipModifiers = false,
            expandMagicUse = false,
            kingdomEventRollMode = "gmroll",
            automateResources = "kingmaker",
            proficiencyMode = "none",
            kingdomEventsTable = null,
            kingdomCultTable = null,
            maximumFamePoints = 3,
            leaderKingdomSkills = RawLeaderKingdomSkills(
                ruler = arrayOf(INDUSTRY, INTRIGUE, POLITICS, STATECRAFT, WARFARE).map { it.value }.toTypedArray(),
                counselor = arrayOf(ARTS, FOLKLORE, POLITICS, SCHOLARSHIP, TRADE).map { it.value }.toTypedArray(),
                emissary = arrayOf(INTRIGUE, MAGIC, POLITICS, STATECRAFT, TRADE).map { it.value }.toTypedArray(),
                general = arrayOf(BOATING, DEFENSE, ENGINEERING, EXPLORATION, WARFARE).map { it.value }.toTypedArray(),
                magister = arrayOf(DEFENSE, FOLKLORE, MAGIC, SCHOLARSHIP, WILDERNESS).map { it.value }.toTypedArray(),
                treasurer = arrayOf(ARTS, BOATING, INDUSTRY, INTRIGUE, TRADE).map { it.value }.toTypedArray(),
                viceroy = arrayOf(AGRICULTURE, ENGINEERING, INDUSTRY, SCHOLARSHIP, WILDERNESS).map { it.value }
                    .toTypedArray(),
                warden = arrayOf(AGRICULTURE, BOATING, DEFENSE, EXPLORATION, WILDERNESS).map { it.value }
                    .toTypedArray(),
            ),
            leaderSkills = RawLeaderSkills(
                ruler = arrayOf(
                    "diplomacy",
                    "deception",
                    "intimidation",
                    "performance",
                    "society",
                    "heraldry",
                    "politics",
                    "ruler"
                ),
                counselor = arrayOf(
                    "diplomacy",
                    "deception",
                    "performance",
                    "religion",
                    "society",
                    "academia",
                    "art",
                    "counselor"
                ),
                emissary = arrayOf(
                    "diplomacy",
                    "deception",
                    "intimidation",
                    "stealth",
                    "thievery",
                    "politics",
                    "underworld",
                    "emissary"
                ),
                general = arrayOf(
                    "diplomacy",
                    "athletics",
                    "crafting",
                    "intimidation",
                    "survival",
                    "scouting",
                    "warfare",
                    "general"
                ),
                magister = arrayOf(
                    "diplomacy",
                    "arcana",
                    "nature",
                    "occultism",
                    "religion",
                    "academia",
                    "scribing",
                    "magister"
                ),
                treasurer = arrayOf(
                    "diplomacy",
                    "crafting",
                    "medicine",
                    "society",
                    "thievery",
                    "labor",
                    "mercantile",
                    "treasurer"
                ),
                viceroy = arrayOf(
                    "diplomacy",
                    "crafting",
                    "medicine",
                    "nature",
                    "society",
                    "architecture",
                    "engineering",
                    "viceroy"
                ),
                warden = arrayOf(
                    "diplomacy",
                    "athletics",
                    "nature",
                    "stealth",
                    "survival",
                    "farming",
                    "hunting",
                    "warden"
                ),
            ),
        ),
        heartlandBlacklist = emptyArray(),
        commodities = RawCurrentCommodities(
            now = RawCommodities(
                food = 0,
                lumber = 0,
                luxuries = 0,
                ore = 0,
                stone = 0,
            ),
            next = RawCommodities(
                food = 0,
                lumber = 0,
                luxuries = 0,
                ore = 0,
                stone = 0,
            ),
        ),
        ruin = RawRuin(
            corruption = RawRuinValues(
                value = 0,
                penalty = 0,
                threshold = 10,
            ),
            crime = RawRuinValues(
                value = 0,
                penalty = 0,
                threshold = 10,
            ),
            decay = RawRuinValues(
                value = 0,
                penalty = 0,
                threshold = 10,
            ),
            strife = RawRuinValues(
                value = 0,
                penalty = 0,
                threshold = 10,
            ),
        ),
        activeSettlement = null,
        turnsWithoutCultEvent = 0,
        turnsWithoutEvent = 0,
        notes = RawNotes(
            gm = "",
            public = ""
        ),
        homebrewMilestones = emptyArray(),
        homebrewActivities = emptyArray(),
        homebrewCharters = emptyArray(),
        homebrewGovernments = emptyArray(),
        homebrewHeartlands = emptyArray(),
        homebrewFeats = emptyArray(),
        activityBlacklist = kingdomActivities
            .filterNot { it.enabled }
            .map { it.id }
            .toTypedArray(),
        modifiers = emptyArray(),
        settlements = emptyArray(),
        leaders = RawLeaders(
            ruler = RawLeaderValues(
                invested = false,
                type = "pc",
                vacant = false,
            ),
            counselor = RawLeaderValues(
                invested = false,
                type = "pc",
                vacant = false,
            ),
            emissary = RawLeaderValues(
                invested = false,
                type = "pc",
                vacant = false,
            ),
            general = RawLeaderValues(
                invested = false,
                type = "pc",
                vacant = false,
            ),
            magister = RawLeaderValues(
                invested = false,
                type = "pc",
                vacant = false,
            ),
            treasurer = RawLeaderValues(
                invested = false,
                type = "pc",
                vacant = false,
            ),
            viceroy = RawLeaderValues(
                invested = false,
                type = "pc",
                vacant = false,
            ),
            warden = RawLeaderValues(
                invested = false,
                type = "pc",
                vacant = false,
            ),
        ),
        charter = RawCharterChoices(
            type = null,
            abilityBoosts = RawAbilityBoostChoices(
                culture = false,
                economy = false,
                loyalty = false,
                stability = false,
            ),
        ),
        heartland = RawHeartlandChoices(
            type = null,
        ),
        government = RawGovernmentChoices(
            type = null,
            abilityBoosts = RawAbilityBoostChoices(
                culture = false,
                economy = false,
                loyalty = false,
                stability = false,
            ),
        ),
        abilityBoosts = RawAbilityBoostChoices(
            culture = false,
            economy = false,
            loyalty = false,
            stability = false,
        ),
        features = emptyArray(),
        bonusFeats = emptyArray(),
        ongoingEvents = emptyArray(),
        groups = emptyArray(),
        skillRanks = RawSkillRanks(
            agriculture = 0,
            arts = 0,
            boating = 0,
            defense = 0,
            engineering = 0,
            exploration = 0,
            folklore = 0,
            industry = 0,
            intrigue = 0,
            magic = 0,
            politics = 0,
            scholarship = 0,
            statecraft = 0,
            trade = 0,
            warfare = 0,
            wilderness = 0,
        ),
        abilityScores = RawAbilityScores(
            culture = 10,
            economy = 10,
            loyalty = 10,
            stability = 10,
        ),
        milestones = kingdomMilestones.map {
            MilestoneChoice(
                id = it.id,
                completed = false,
                enabled = it.enabledOnFirstRun,
            )
        }.toTypedArray(),
        charterBlacklist = emptyArray(),
        governmentBlacklist = emptyArray(),
    )
