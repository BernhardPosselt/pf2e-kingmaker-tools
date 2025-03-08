package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill.*
import at.posselt.pfrpg2e.kingdom.AutomateResources
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.KingdomSettings
import at.posselt.pfrpg2e.kingdom.RawLeaderKingdomSkills
import at.posselt.pfrpg2e.kingdom.RawLeaderSkills
import at.posselt.pfrpg2e.kingdom.data.RawLeaderValues
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.UntrainedProficiencyMode
import at.posselt.pfrpg2e.settings.getBoolean
import at.posselt.pfrpg2e.settings.getInt
import at.posselt.pfrpg2e.settings.getString
import at.posselt.pfrpg2e.settings.registerInt
import at.posselt.pfrpg2e.settings.registerScalar
import at.posselt.pfrpg2e.toCamelCase
import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actor.PF2ECharacter
import com.foundryvtt.pf2e.actor.PF2ENpc

private fun migrateLeaders(game: Game, values: RawLeaderValues) {
    val name = values.asDynamic().name.unsafeCast<String?>()
    values.type = "pc"
    values.uuid = name
        ?.let { n -> game.actors.find { it.name == n && (it is PF2ECharacter || it is PF2ENpc) }?.uuid }
}

class Migration12 : Migration(12, true) {
    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        kingdom.activityBlacklist = (kingdom.activityBlacklist + arrayOf(
            "focused-attention-vk",
            "celebrate-holiday-vk",
            "retrain-vk",
            "fortify-hex-vk",
            "clear-hex-vk",
            "new-leadership-vk"
        )).distinct().toTypedArray()
        // register old settings
        game.settings.registerInt("rpToXpConversionRate", default = 1, name = "", hidden = true)
        game.settings.registerInt("rpToXpConversionLimit", default = 120, name = "", hidden = true)
        game.settings.registerInt("xpPerClaimedHex", default = 10, name = "", hidden = true)
        game.settings.registerScalar<String>("automateResources", default = "Kingmaker", name = "", hidden = true)
        game.settings.registerScalar<Boolean>(
            "kingdomSkillIncreaseEveryLevel",
            default = false,
            name = "",
            hidden = true
        )
        game.settings.registerScalar<Boolean>("kingdomAlwaysAddHalfLevel", default = false, name = "", hidden = true)
        game.settings.registerScalar<Boolean>("kingdomAlwaysAddLevel", default = false, name = "", hidden = true)
        game.settings.registerScalar<Boolean>(
            "reduceDCToBuildLumberStructures",
            default = false,
            name = "",
            hidden = true
        )
        game.settings.registerScalar<Boolean>(
            "kingdomAllStructureItemBonusesStack",
            default = false,
            name = "",
            hidden = true
        )
        game.settings.registerScalar<Boolean>(
            "kingdomIgnoreSkillRequirements",
            default = false,
            name = "",
            hidden = true
        )
        game.settings.registerScalar<Boolean>("autoCalculateArmyConsumption", default = false, name = "", hidden = true)
        game.settings.registerScalar<Boolean>("vanceAndKerensharaXP", default = false, name = "", hidden = true)
        game.settings.registerScalar<Boolean>("capitalInvestmentInCapital", default = false, name = "", hidden = true)
        game.settings.registerScalar<Boolean>("cultOfTheBloomEvents", default = false, name = "", hidden = true)
        game.settings.registerScalar<Boolean>("autoCalculateSettlementLevel", default = true, name = "", hidden = true)
        game.settings.registerScalar<String>("kingdomEventRollMode", default = "gmroll", name = "", hidden = true)
        game.settings.registerScalar<String>("kingdomEventsTable", default = "", name = "", hidden = true)
        game.settings.registerScalar<String>("kingdomCultTable", default = "", name = "", hidden = true)


        // migrate settings
        val automateResources = when (game.settings.getString("automateResources")) {
            "tileBased" -> AutomateResources.TILE_BASED.toCamelCase()
            "manual" -> AutomateResources.MANUAL.toCamelCase()
            else -> AutomateResources.KINGMAKER.toCamelCase()
        }
        val proficiencyMode = if (game.settings.getBoolean("kingdomAlwaysAddLevel")) {
            UntrainedProficiencyMode.FULL.toCamelCase()
        } else if (game.settings.getBoolean("kingdomAlwaysAddHalfLevel")) {
            UntrainedProficiencyMode.HALF.toCamelCase()
        } else {
            UntrainedProficiencyMode.NONE.toCamelCase()
        }
        val kingdomEventsTableUuid = game.settings.getString("kingdomEventsTable")
            .takeIf { it.isNotBlank() }
            ?.let { game.tables.getName(it)?.uuid }
        val kingdomCultTable = game.settings.getString("kingdomCultTable")
            .takeIf { it.isNotBlank() }
            ?.let { game.tables.getName(it)?.uuid }
        migrateLeaders(game, kingdom.leaders.ruler)
        migrateLeaders(game, kingdom.leaders.counselor)
        migrateLeaders(game, kingdom.leaders.emissary)
        migrateLeaders(game, kingdom.leaders.general)
        migrateLeaders(game, kingdom.leaders.magister)
        migrateLeaders(game, kingdom.leaders.treasurer)
        migrateLeaders(game, kingdom.leaders.viceroy)
        migrateLeaders(game, kingdom.leaders.warden)
        kingdom.settings = KingdomSettings(
            maximumFamePoints = kingdom.fame.asDynamic().max,
            rpToXpConversionRate = game.settings.getInt("rpToXpConversionRate"),
            rpToXpConversionLimit = game.settings.getInt("rpToXpConversionLimit"),
            resourceDicePerVillage = 0,
            automateStats = false,
            resourceDicePerTown = 0,
            resourceDicePerCity = 0,
            resourceDicePerMetropolis = 0,
            xpPerClaimedHex = game.settings.getInt("xpPerClaimedHex"),
            includeCapitalItemModifier = true,
            cultOfTheBloomEvents = game.settings.getBoolean("cultOfTheBloomEvents"),
            autoCalculateSettlementLevel = game.settings.getBoolean("autoCalculateSettlementLevel"),
            vanceAndKerensharaXP = game.settings.getBoolean("vanceAndKerensharaXP"),
            capitalInvestmentInCapital = game.settings.getBoolean("capitalInvestmentInCapital"),
            reduceDCToBuildLumberStructures = game.settings.getBoolean("reduceDCToBuildLumberStructures"),
            kingdomSkillIncreaseEveryLevel = game.settings.getBoolean("kingdomSkillIncreaseEveryLevel"),
            kingdomAllStructureItemBonusesStack = game.settings.getBoolean("kingdomAllStructureItemBonusesStack"),
            kingdomIgnoreSkillRequirements = game.settings.getBoolean("kingdomIgnoreSkillRequirements"),
            autoCalculateArmyConsumption = game.settings.getBoolean("autoCalculateArmyConsumption"),
            enableLeadershipModifiers = false,
            expandMagicUse = kingdom.settings.expandMagicUse,
            kingdomEventRollMode = game.settings.getString("kingdomEventRollMode"),
            automateResources = automateResources,
            proficiencyMode = proficiencyMode,
            kingdomEventsTable = kingdomEventsTableUuid,
            kingdomCultTable = kingdomCultTable,
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
        )
    }
}