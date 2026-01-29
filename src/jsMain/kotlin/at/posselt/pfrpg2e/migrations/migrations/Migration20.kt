package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.camping.CampingActivity
import at.posselt.pfrpg2e.camping.CampingData
import at.posselt.pfrpg2e.utils.toMutableRecord
import com.foundryvtt.core.Game
import kotlinx.js.JsPlainObject

@JsPlainObject
private external interface OldCampingActivity {
    var activityId: String
    var actorUuid: String?
    var result: String?
    var selectedSkill: String?
}


class Migration20 : Migration(20) {
    override suspend fun migrateCamping(game: Game, camping: CampingData) {
        camping.campingActivities = camping.campingActivities.unsafeCast<Array<OldCampingActivity>>()
            .map {
                it.activityId to CampingActivity(
                    actorUuid = it.actorUuid,
                    result = it.result,
                    selectedSkill = it.selectedSkill,
                )
            }
            .toMutableRecord()
    }
}