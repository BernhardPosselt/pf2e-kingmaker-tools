@file:JsQualifier("foundry.documents")
package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import kotlin.js.Promise

open external class Item : ClientDocument {
    companion object : DocumentStatic<Item>

    override fun delete(operation: DatabaseDeleteOperation): Promise<Item>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<Item?>

    val actor: Actor?
    val thumbnail: String
    val isOwned: Boolean

    var _id: String?
    var name: String?
    var type: String
    var img: String
    var folder: Folder?
    var sort: Int
    var ownership: Ownership
}
