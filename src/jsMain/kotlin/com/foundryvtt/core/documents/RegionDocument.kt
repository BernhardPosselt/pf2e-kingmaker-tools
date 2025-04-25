@file:JsQualifier("foundry.documents")
package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.data.BaseShapeData
import com.foundryvtt.core.utils.Color
import kotlin.js.Promise

external class RegionDocument : ClientDocument {
    companion object : DocumentStatic<RegionDocument>

    override fun delete(operation: DatabaseDeleteOperation): Promise<RegionDocument>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<RegionDocument?>

    var _id: String
    var name: String
    var color: Color
    var shapes: Array<BaseShapeData>
    var behaviors: Array<RegionBehavior>
    var elevation: Int
    var visibility: Int
    var locked: Boolean
}