package at.posselt.pfrpg2e.migrations

import at.posselt.pfrpg2e.camping.CampingActor
import at.posselt.pfrpg2e.camping.getCamping
import at.posselt.pfrpg2e.camping.getCampingActors
import at.posselt.pfrpg2e.camping.setCamping
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.getKingdomActors
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.migrations.migrations.Migration17
import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.utils.isFirstGM
import at.posselt.pfrpg2e.utils.openJournal
import at.posselt.pfrpg2e.utils.t
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
    Migration17(),
)

private val latestMigrationVersion = migrations.maxOfOrNull { it.version }!!

suspend fun Game.migratePfrpg2eKingdomCampingWeather() {
    val currentVersion = settings.pfrpg2eKingdomCampingWeather.getSchemaVersion()
        .takeIf { it != 0 }
        ?: latestMigrationVersion
    console.log("${t("moduleName")}: ${t("migrations.upgradingFromTo", recordOf("fromVersion" to currentVersion, "toVersion" to latestMigrationVersion))}")
    if (currentVersion < 16) {
        ui.notifications.error(
            "${t("moduleName")}: ${t("migrations.unsupportedVersions", recordOf("version" to "4.8.2 (FoundryVTT V12)"))}"
        )
    } else if (isFirstGM() && currentVersion < latestMigrationVersion) {
        try {
            migrateFrom(currentVersion)
        } catch (e: Throwable) {
            console.log(e)
            ui.notifications.error(t("migrations.failed", recordOf("version" to currentVersion)))
            throw e
        }
    }
}

private suspend fun Game.migrateFrom(currentVersion: Int) {
    ui.notifications.info("${t("moduleName")}: ${t("migrations.doNotClose")}")
    // create backups
    val kingdomActors = getKingdomActors()
    val campingActors = getCampingActors()
    createBackups(this, kingdomActors, campingActors, currentVersion)

    val migrationsToRun = migrations.filter { it.version > currentVersion }

    migrationsToRun
        .forEach { migration ->
            ui.notifications.info(
                "${t("moduleName")}: ${
                    t(
                        "migrations.runningMigration",
                        recordOf("version" to migration.version)
                    )
                }"
            )
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
    ui.notifications.info("${t("moduleName")}: ${t("migrations.successful")}")

    if (migrationsToRun.any { it.showUpgradingNotices }) {
        openJournal("Compendium.pf2e-kingmaker-tools.kingmaker-tools-journals.JournalEntry.wz1mIWMxDJVsMIUd")
    }
}

