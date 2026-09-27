@file:JsQualifier("foundry.documents")
package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.abstract.Document
import com.foundryvtt.core.abstract.DocumentConstructionContext
import kotlin.js.Promise

external class NoteDocument(
    data: AnyObject = definedExternally,
    options: DocumentConstructionContext = definedExternally
) : Document {
    companion object : DocumentStatic<NoteDocument>;

    override fun delete(operation: DatabaseDeleteOperation): Promise<NoteDocument>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<NoteDocument?>

    var _id: String
    var entryId: String
    var pageId: String
    var x: Double
    var y: Double
    var elevation: Int
    var sort: Int
    var texture: TextureData
    var iconSize: Int
    var text: String
    var fontFamily: String
    var fontSize: Int
    var textAnchor: Int
    var textColor: String
    var global: Boolean
}

