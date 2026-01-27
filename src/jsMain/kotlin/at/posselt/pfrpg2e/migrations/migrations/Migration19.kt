package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.camping.CampingData
import at.posselt.pfrpg2e.camping.RestSettings
import com.foundryvtt.core.Game


class Migration19 : Migration(19) {
    override suspend fun migrateCamping(game: Game, camping: CampingData) {
        if (camping.autoApplyFatigued.unsafeCast<Boolean?>() == null) {
            camping.autoApplyFatigued = true
        }
        if (camping.restSettings.unsafeCast<RestSettings?>() == null) {
            camping.restSettings = RestSettings(
                skipWatch = false,
                skipDailyPreparations = false,
                disableRandomEncounter = false,
                skipWeather = false,
            )
        }
        if (camping.secondsSpentTraveling.unsafeCast<Int?>() == null) {
            camping.secondsSpentTraveling = 0
        }
        if (camping.secondsSpentHexploring.unsafeCast<Int?>() == null) {
            camping.secondsSpentHexploring = 0
        }
        if (camping.resetTimeTrackingAfterOneDay.unsafeCast<Boolean?>() == null) {
            camping.resetTimeTrackingAfterOneDay = true
        }
        if (camping.travelModeActive.unsafeCast<Boolean?>() == null) {
            camping.travelModeActive = false
        }
    }
}