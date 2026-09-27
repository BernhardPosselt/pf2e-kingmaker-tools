@file:JsQualifier("foundry.documents")
package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import kotlin.js.Promise


external class DrawingDocument : ClientDocument {
    companion object : DocumentStatic<DrawingDocument>

    override fun delete(operation: DatabaseDeleteOperation): Promise<DrawingDocument>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<DrawingDocument?>

    val isAuthor: Boolean

    var _id: String
    var author: User?
    var shape: ShapeData
    var x: Double
    var y: Double
    var elevation: Int
    var sort: Int
    var rotation: Int
    var bezierFactor: Double
    var fillType: Int
    var fillColor: Int
    var fillAlpha: Double
    var strokeWidth: Int
    var strokeColor: String
    var texture: TextureData?
    var text: String
    var fontFamily: String
    var fontSize: Int
    var textColor: String
    var textAlpha: Double
    var hidden: Boolean
    var locked: Boolean
    var `interface`: Boolean
}
