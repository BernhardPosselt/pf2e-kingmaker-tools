package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.helpers.CreateDocumentCallback
import com.foundryvtt.core.helpers.DeleteDocumentCallback
import com.foundryvtt.core.helpers.HooksEventListener
import com.foundryvtt.core.helpers.PreCreateDocumentCallback
import com.foundryvtt.core.helpers.PreDeleteDocumentCallback
import com.foundryvtt.core.helpers.PreUpdateDocumentCallback
import com.foundryvtt.core.helpers.UpdateDocumentCallback
import js.objects.unsafeJso
import kotlin.js.Promise


@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun Item.update(data: Item, operation: DatabaseUpdateOperation = unsafeJso()): Promise<Item?> =
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