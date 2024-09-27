package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.actor.npcs
import at.posselt.pfrpg2e.camping.ActorMeal
import at.posselt.pfrpg2e.camping.CampingData
import at.posselt.pfrpg2e.camping.CampingSkill
import at.posselt.pfrpg2e.camping.dialogs.Track
import at.posselt.pfrpg2e.camping.getDefaultCamping
import at.posselt.pfrpg2e.combattracks.getCombatTrack
import at.posselt.pfrpg2e.combattracks.setCombatTrack
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.KingdomSettings
import at.posselt.pfrpg2e.kingdom.getParsedStructureData
import at.posselt.pfrpg2e.settings.*
import at.posselt.pfrpg2e.utils.typeSafeUpdate
import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actor.PF2ENpc

private fun migrateCombatTrack(game: Game, combatTrack: dynamic): Track? {
    val trackName = combatTrack["name"].unsafeCast<String?>()
    return trackName
        ?.let { game.playlists.getName(it) }
        ?.uuid
        ?.let { Track(playlistUuid = it) }
}

private val structureNamesToMigrate = setOf(
    "Bridge, Stone",
    "Bridge",
    "Gladiatorial Arena",
    "Magical Streetlamps",
    "Paved Streets",
    "Printing House",
    "Sewer System",
    "Wall, Stone",
    "Wall, Wooden",
)
private val replacements = mapOf(
    "Broiled Tuskwater Oysters" to "Broiled Oysters",
    "First World Mince Pie" to "Supernatural Mince Pie",
    "Galt Ragout" to "Ragout",
    "Giant Scrambled Egg With Shambletus" to "Giant Scrambled Egg",
    "Berry Pie" to "Pie",
    "Whiterose Oysters" to "Oysters",
    "Owlbear Omelet" to "Omelet",
)

private fun parseDcValue(activity: dynamic): Int? = when (val activityDc = activity.dc) {
    "zone" -> null
    "actorLevel" -> null
    null -> null
    else -> activityDc as Int
}

private fun parseDcType(activity: dynamic): String = when (activity.dc) {
    "zone" -> "zone"
    "actorLevel" -> "actorLevel"
    null -> "none"
    else -> "static"
}

private val activityBlacklistMappings = mapOf(
    "restore-the-temple-of-the-elk" to "restore-the-temple",
    "harvest-azure-lily-pollen" to "harvest-lily-pollen",
)

class Migration10 : Migration(10) {
    override suspend fun migrateCamping(game: Game, camping: CampingData) {
        game.settings.registerScalar<String>(
            "proxyEncounterTable",
            default = "",
            name = "ProxyEncounterTable",
        )
        game.settings.registerScalar<String>(
            "randomEncounterRollMode",
            default = "gmroll",
            name = "ProxyEncounterTable",
        )
        camping.randomEncounterRollMode = game.settings.getString("randomEncounterRollMode").ifBlank { "gmroll" }
        val proxyTableName = game.settings.getString("proxyEncounterTable")
        camping.proxyRandomEncounterTableUuid = game.tables.getName(proxyTableName)?.uuid
        camping.regionSettings = getDefaultCamping(game).regionSettings
        camping.cooking.knownRecipes = camping.cooking.knownRecipes.mapNotNull {
            if (it in replacements) {
                replacements[it]
            } else {
                it
            }
        }.toTypedArray()
        camping.section = "prepareCampsite"
        val homebrewCampingActivities = camping.homebrewCampingActivities.unsafeCast<Array<dynamic>>()
        val newSkills: Map<String, Array<CampingSkill>> = homebrewCampingActivities.associate { activity ->
            val activityName = activity.name as String
            val dc = parseDcValue(activity)
            val dcType = parseDcType(activity)
            if (activity.skills == "any") {
                activityName to arrayOf(
                    CampingSkill(
                        name = "any",
                        proficiency = "untrained",
                        dcType = dcType,
                        dc = dc,
                    )
                )
            } else {
                activityName to activity.skills.map { skill: String ->
                    CampingSkill(
                        name = skill,
                        proficiency = activity.skillRequirements.find { req ->
                            req.skill == skill
                        }?.proficiency ?: "untrained",
                        dcType = dcType,
                        dc = dc,
                    )
                }
            }
        }
        camping.homebrewCampingActivities.forEach { activity ->
            activity.skills = newSkills[activity.name] ?: emptyArray<CampingSkill>()
        }
        camping.cooking.minimumSubsistence = 0
        camping.restRollMode = if (camping.restRollMode == "one-every-4-hours") {
            "oneEveryFourHours"
        } else {
            camping.restRollMode
        }
        camping.alwaysPerformActivities = emptyArray()
        camping.cooking.actorMeals = camping.cooking.actorMeals.map {
            ActorMeal(
                actorUuid = it.actorUuid,
                favoriteMeal = it.favoriteMeal,
                chosenMeal = "nothing"
            )
        }.toTypedArray()
        camping.cooking.results = emptyArray()
        camping.currentRegion = "Zone 00"
    }

    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        val replacements = kingdom.activityBlacklist
            .filter { activityBlacklistMappings.contains(it) }
            .mapNotNull { activityBlacklistMappings[it] }
            .toTypedArray()
        kingdom.activityBlacklist = kingdom.activityBlacklist
            .filter { !activityBlacklistMappings.contains(it) }.toTypedArray() + replacements
        kingdom.fame.max = 3
        kingdom.settings = KingdomSettings(
            expandMagicUse = false,
        )
    }

    override suspend fun migrateOther(game: Game) {
        // migrate combat tracks
        for (scene in game.scenes) {
            scene.getCombatTrack()?.let { track: dynamic ->
                val combatTrack = migrateCombatTrack(game, track)
                scene.setCombatTrack(combatTrack)
            }
            for (token in scene.tokens) {
                val actor = token.actor
                actor?.getCombatTrack()?.let { track: dynamic ->
                    val combatTrack = migrateCombatTrack(game, track)
                    actor.setCombatTrack(combatTrack)
                }
            }
        }
        for (actor in game.actors) {
            actor.getCombatTrack()?.let { track: dynamic ->
                val combatTrack = migrateCombatTrack(game, track)
                actor.setCombatTrack(combatTrack)
            }
        }
        // migrate token images
        for (actor in game.npcs()) {
            val data = actor.getParsedStructureData()
            val name = data?.name
            if (name != null && name in structureNamesToMigrate) {
                val path = "modules/pf2e-kingmaker-tools/img/structures/${name}.webp"
                actor.typeSafeUpdate {
                    prototypeToken.texture.src = path
                    img = path
                }
            }
        }
        val tokensToMigrate = game.scenes.contents
            .flatMap { it.tokens.contents.toList() }
            .filter { it.actor is PF2ENpc }
        for (token in tokensToMigrate) {
            val actor = token.actor as PF2ENpc
            val data = actor.getParsedStructureData()
            val name = data?.name
            if (name != null && name in structureNamesToMigrate) {
                token.typeSafeUpdate {
                    texture.src = "modules/pf2e-kingmaker-tools/img/structures/${name}.webp"
                }
            }
        }
    }
}