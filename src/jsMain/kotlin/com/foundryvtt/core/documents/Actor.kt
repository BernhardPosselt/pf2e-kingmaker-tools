package com.foundryvtt.core

import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.collections.EmbeddedCollection
import com.foundryvtt.core.documents.*
import js.objects.jso
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
    var prototypeToken: TokenDocument
    var items: EmbeddedCollection<Item>

    // var effects: EmbeddedCollection<Effect>
    var folder: Folder?
    var sort: Int
    var ownership: Ownership
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun Actor.update(data: Actor, operation: DatabaseUpdateOperation = jso()): Promise<Actor?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateActor(callback: PreCreateDocumentCallback<Actor, O>) =
    on("preCreateActor", callback)

fun <O> HooksEventListener.onPreUpdateActor(callback: PreUpdateDocumentCallback<Actor, O>): Unit =
    on("preUpdateActor", callback)

fun <O> HooksEventListener.onPreDeleteActor(callback: PreDeleteDocumentCallback<Actor, O>) =
    on("preDeleteActor", callback)

fun <O> HooksEventListener.onCreateActor(callback: CreateDocumentCallback<Actor, O>) =
    on("createActor", callback)

fun <O> HooksEventListener.onUpdateActor(callback: UpdateDocumentCallback<Actor, O>) =
    on("updateActor", callback)

fun <O> HooksEventListener.onDeleteActor(callback: DeleteDocumentCallback<Actor, O>) =
    on("deleteActor", callback)