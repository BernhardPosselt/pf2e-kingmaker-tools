package com.foundryvtt.core.documents

import com.foundryvtt.core.*
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import js.objects.jso
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

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun Item.update(data: Item, operation: DatabaseUpdateOperation = jso()): Promise<Item?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateItem(callback: PreCreateDocumentCallback<Item, O>) =
    on("preCreateItem", callback)

fun <O> HooksEventListener.onPreUpdateItem(callback: PreUpdateDocumentCallback<Item, O>): Unit =
    on("preUpdateItem", callback)

fun <O> HooksEventListener.onPreDeleteItem(callback: PreDeleteDocumentCallback<Item, O>) =
    on("preDeleteItem", callback)

fun <O> HooksEventListener.onCreateItem(callback: CreateDocumentCallback<Item, O>) =
    on("createItem", callback)

fun <O> HooksEventListener.onUpdateItem(callback: UpdateDocumentCallback<Item, O>) =
    on("updateItem", callback)

fun <O> HooksEventListener.onDeleteItem(callback: DeleteDocumentCallback<Item, O>) =
    on("deleteItem", callback)