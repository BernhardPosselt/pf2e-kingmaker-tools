@file:JsQualifier("foundry.documents")
package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import kotlin.js.Promise

external class TileDocument : ClientDocument {
    companion object : DocumentStatic<TileDocument>

    override fun delete(operation: DatabaseDeleteOperation): Promise<TileDocument>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<TileDocument?>

    var _id: String
    var texture: TextureData
    var width: Int
    var height: Int
    var x: Double
    var y: Double
    var elevation: Int
    var sort: Int
    var rotation: Int
    var alpha: Double
    var hidden: Boolean
    var locked: Boolean
    var restrictions: TileRestrictions
    var occlusion: TileOcclusion
    var video: TileVideo
}
