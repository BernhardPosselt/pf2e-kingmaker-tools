@file:JsQualifier("foundry.documents")
package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.documents.collections.EmbeddedCollection
import kotlin.js.Promise


open external class Actor : ClientDocument {
    companion object : DocumentStatic<Actor>

    override fun delete(operation: DatabaseDeleteOperation): Promise<Actor>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<Actor?>

    // schema
    var _id: String
    var name: String
    var img: String?
    var type: String
    var prototypeToken: PrototypeToken
    var items: EmbeddedCollection<Item>

    // var effects: EmbeddedCollection<Effect>
    var folder: Folder?
    var sort: Int
    var ownership: Ownership
    val baseActor: Actor?
    val token: TokenDocument?
}

