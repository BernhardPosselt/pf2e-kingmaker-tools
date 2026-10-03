package at.posselt.pfrpg2e.camping

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import at.posselt.pfrpg2e.utils.buildSchema

@JsExport
class CampingSheetDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            boolean("travelModeActive")
            boolean("forcedMarchActive")
            string("region")
            schema("activities") {
                stringRecord("selectedSkill")
                stringRecord("degreeOfSuccess") {
                    string(nullable = true) {

                    }
                }
            }
            schema("recipes") {
                stringRecord("selectedSkill")
                stringRecord("degreeOfSuccess") {
                    string(nullable = true) {

                    }
                }
            }
        }
    }
}
