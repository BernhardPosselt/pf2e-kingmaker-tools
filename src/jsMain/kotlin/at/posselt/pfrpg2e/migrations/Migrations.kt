package at.posselt.pfrpg2e.migrations

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.actor.npcs
import at.posselt.pfrpg2e.camping.CampingActor
import at.posselt.pfrpg2e.camping.getCamping
import at.posselt.pfrpg2e.camping.getCampingActors
import at.posselt.pfrpg2e.camping.setCamping
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.getKingdomActors
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.migrations.migrations.Migration10
import at.posselt.pfrpg2e.migrations.migrations.Migration11
import at.posselt.pfrpg2e.migrations.migrations.Migration12
import at.posselt.pfrpg2e.migrations.migrations.Migration13
import at.posselt.pfrpg2e.migrations.migrations.Migration14
import at.posselt.pfrpg2e.migrations.migrations.Migration15
import at.posselt.pfrpg2e.migrations.migrations.Migration9
import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.utils.getAppFlag
import at.posselt.pfrpg2e.utils.isFirstGM
import at.posselt.pfrpg2e.utils.openJournal
import at.posselt.pfrpg2e.utils.setAppFlag
import at.posselt.pfrpg2e.utils.toRecord
import com.foundryvtt.core.Game
import com.foundryvtt.core.ui
import com.foundryvtt.core.utils.deepClone
import com.foundryvtt.pf2e.actor.PF2ENpc
import com.foundryvtt.pf2e.actor.PF2EParty
import js.objects.recordOf
import kotlinx.coroutines.await
import kotlin.math.max

private suspend fun createBackups(
    game: Game,
    kingdomActors: List<KingdomActor>,
    campingActors: List<CampingActor>,
    currentVersion: Int
) {
    val backup = recordOf(
        "version" to currentVersion,
        "camping" to campingActors.map {
            it.id!! to it.getCamping()
        }.toRecord(),
        "kingdoms" to kingdomActors.map {
            it.id!! to it.getKingdom()
        }.toRecord()
    )
    game.settings.pfrpg2eKingdomCampingWeather.setLatestMigrationBackup(
        JSON.stringify(backup)
    )
}

private val migrations = listOf(
    Migration9(),
    Migration10(),
    Migration11(),
    Migration12(),
    Migration13(),
    Migration14(),
    Migration15(),
)

val latestMigrationVersion = migrations.maxOfOrNull { it.version }!!

suspend fun createPartyActor(index: Int): PF2EParty {
    return PF2EParty.create(
        recordOf(
            "type" to "party",
            "name" to "Party $index",
            "ownership" to recordOf(
                "default" to 3
            )
        )
    ).await()
}

suspend fun Game.migratePfrpg2eKingdomCampingWeather() {
    val existingVersion = settings.pfrpg2eKingdomCampingWeather.getSchemaVersion()
    val currentVersion = if (existingVersion == 0) {
        // fix version
        val version = if (getKingdomActors().any { it.getKingdom()?.homebrewKingdomEvents == null }) {
            13
        } else {
            latestMigrationVersion
        }
        settings.pfrpg2eKingdomCampingWeather.setSchemaVersion(latestMigrationVersion)
        version
    } else {
        existingVersion
    }
    console.log("${Config.moduleName}: Upgrading from $currentVersion to $latestMigrationVersion")
    if (currentVersion < 9) {
        ui.notifications.error(
            "${Config.moduleName}: Upgrades from versions prior to 1.1.1 are not supported anymore. " +
                    "Please upgrade to 1.1.1 first"
        )
        return
    }
    if (isFirstGM() && currentVersion < latestMigrationVersion) {
        ui.notifications.info("${Config.moduleName}: Running migrations, please do not close the window")

        // special handling needed to copy actors onto party actor
        // this can be removed in Foundry 14
        if (currentVersion < 13) {
            val parties = actors.contents.filterIsInstance<PF2EParty>()
            val kingdoms = npcs().mapNotNull { it.getAppFlag<PF2ENpc, KingdomData?>("kingdom-sheet") }
            val camps = npcs().mapNotNull { it.getAppFlag<PF2ENpc, KingdomData?>("camping-sheet") }
            val sheets = max(kingdoms.size, camps.size)
            if (sheets > 0) {
                for (i in 0..(sheets - 1)) {
                    val party = parties.getOrNull(i)
                    val kingdom = kingdoms.getOrNull(i)
                    val camping = camps.getOrNull(i)
                    val target = party ?: createPartyActor(i)
                    if (kingdom != null) {
                        target.setAppFlag("kingdom-sheet", deepClone(kingdom))
                    }
                    if (camping != null) {
                        target.setAppFlag("camping-sheet", deepClone(camping))
                    }
                }
            }
        }
        // create backups
        val kingdomActors = getKingdomActors()
        val campingActors = getCampingActors()
        createBackups(this, kingdomActors, campingActors, currentVersion)

        val migrationsToRun = migrations.filter { it.version > currentVersion }

        migrationsToRun
            .forEach { migration ->
                ui.notifications.info("Running migration ${Config.moduleName} version ${migration.version}")
                campingActors.forEach { actor ->
                    actor.getCamping()?.let { camping ->
                        migration.migrateCamping(this, camping)
                        actor.setCamping(camping)
                    }
                }

                kingdomActors.forEach { actor ->
                    actor.getKingdom()?.let { kingdom ->
                        migration.migrateKingdom(this, kingdom)
                        actor.setKingdom(kingdom)
                    }
                }

                migration.migrateOther(this)
            }

        settings.pfrpg2eKingdomCampingWeather.setSchemaVersion(latestMigrationVersion)
        ui.notifications.info("Kingdom Building, Camping & Weather: Migration successful")

        if (migrationsToRun.any { it.showUpgradingNotices }) {
            openJournal("Compendium.pf2e-kingmaker-tools.kingmaker-tools-journals.JournalEntry.wz1mIWMxDJVsMIUd")
        }
    }
}