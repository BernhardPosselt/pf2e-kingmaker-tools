package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.CreateDocumentCallback
import com.foundryvtt.core.DeleteDocumentCallback
import com.foundryvtt.core.HooksEventListener
import com.foundryvtt.core.PreCreateDocumentCallback
import com.foundryvtt.core.PreDeleteDocumentCallback
import com.foundryvtt.core.PreUpdateDocumentCallback
import com.foundryvtt.core.UpdateDocumentCallback
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import js.objects.jso
import kotlin.js.Promise


@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun JournalEntry.update(data: JournalEntry, operation: DatabaseUpdateOperation = jso()): Promise<JournalEntry?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateJournalEntry(callback: PreCreateDocumentCallback<JournalEntry, O>) =
    on("preCreateJournalEntry", callback)

fun <O> HooksEventListener.onPreUpdateJournalEntry(callback: PreUpdateDocumentCallback<JournalEntry, O>): Unit =
    on("preUpdateJournalEntry", callback)

fun <O> HooksEventListener.onPreDeleteJournalEntry(callback: PreDeleteDocumentCallback<JournalEntry, O>) =
    on("preDeleteJournalEntry", callback)

fun <O> HooksEventListener.onCreateJournalEntry(callback: CreateDocumentCallback<JournalEntry, O>) =
    on("createJournalEntry", callback)

fun <O> HooksEventListener.onUpdateJournalEntry(callback: UpdateDocumentCallback<JournalEntry, O>) =
    on("updateJournalEntry", callback)

fun <O> HooksEventListener.onDeleteJournalEntry(callback: DeleteDocumentCallback<JournalEntry, O>) =
    on("deleteJournalEntry", callback)