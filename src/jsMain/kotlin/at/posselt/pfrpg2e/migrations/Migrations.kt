package at.posselt.pfrpg2e.migrations

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.actor.npcs
import at.posselt.pfrpg2e.camping.CampingActor
import at.posselt.pfrpg2e.camping.getCamping
import at.posselt.pfrpg2e.camping.getCampingActors
import at.posselt.pfrpg2e.camping.setCamping
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.migrations.migrations.Migration10
import at.posselt.pfrpg2e.migrations.migrations.Migration11
import at.posselt.pfrpg2e.migrations.migrations.Migration12
import at.posselt.pfrpg2e.migrations.migrations.Migration13
import at.posselt.pfrpg2e.migrations.migrations.Migration6
import at.posselt.pfrpg2e.migrations.migrations.Migration7
import at.posselt.pfrpg2e.migrations.migrations.Migration9
import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.utils.isFirstGM
import at.posselt.pfrpg2e.utils.openJournal
import at.posselt.pfrpg2e.utils.toRecord
import com.foundryvtt.core.Game
import com.foundryvtt.core.ui
import js.objects.recordOf

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
    Migration6(),
    Migration7(),
    Migration9(),
    Migration10(),
    Migration11(),
    Migration12(),
    Migration13(),
)

val latestMigrationVersion = migrations.maxOfOrNull { it.version }!!

suspend fun Game.migratePfrpg2eKingdomCampingWeather() {
    val currentVersion = settings.pfrpg2eKingdomCampingWeather.getSchemaVersion()
    console.log("${Config.moduleName}: Upgrading from $currentVersion to $latestMigrationVersion")
    if (currentVersion < 6) {
        ui.notifications.error(
            "${Config.moduleName}: Upgrades from versions prior to 0.12.2 are not supported anymore. " +
                    "Please upgrade to 1.1.1 first"
        )
        return
    }
    if (isFirstGM() && currentVersion < latestMigrationVersion) {
        ui.notifications.info("${Config.moduleName}: Running migrations, please do not close the window")

        // create backups
        val kingdomActors = npcs().filter { it.getKingdom() != null }
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