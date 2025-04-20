package at.posselt.pfrpg2e.camping

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.data.dsl.buildSchema

class CampingSheetDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            string("region")
            schema("activities") {
                record("selectedSkill")
                record("degreeOfSuccess", nullable = true)
            }
            schema("recipes") {
                record("selectedSkill")
                record("degreeOfSuccess", nullable = true)
            }
        }
    }
}
