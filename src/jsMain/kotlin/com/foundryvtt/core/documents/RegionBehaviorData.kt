@file:JsQualifier("foundry.documents")
package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import kotlin.js.Promise

external class RegionBehavior : ClientDocument {
    companion object : DocumentStatic<RegionDocument>

    override fun delete(operation: DatabaseDeleteOperation): Promise<RegionDocument>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<RegionDocument?>
    var _id: String
    var name: String
    var type: String
    var system: AnyObject
    var disabled: Boolean
}