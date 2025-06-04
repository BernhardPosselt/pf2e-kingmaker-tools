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
fun User.update(data: User, operation: DatabaseUpdateOperation = unsafeJso()): Promise<User?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateUser(callback: PreCreateDocumentCallback<User, O>) =
    on("preCreateUser", callback)

fun <O> HooksEventListener.onPreUpdateUser(callback: PreUpdateDocumentCallback<User, O>): Unit =
    on("preUpdateUser", callback)

fun <O> HooksEventListener.onPreDeleteUser(callback: PreDeleteDocumentCallback<User, O>) =
    on("preDeleteUser", callback)

fun <O> HooksEventListener.onCreateUser(callback: CreateDocumentCallback<User, O>) =
    on("createUser", callback)

fun <O> HooksEventListener.onUpdateUser(callback: UpdateDocumentCallback<User, O>) =
    on("updateUser", callback)

fun <O> HooksEventListener.onDeleteUser(callback: DeleteDocumentCallback<User, O>) =
    on("deleteUser", callback)