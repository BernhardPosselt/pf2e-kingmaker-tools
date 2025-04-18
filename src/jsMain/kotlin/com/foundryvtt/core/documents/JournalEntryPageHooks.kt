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
fun JournalEntryPage.update(
    data: JournalEntryPage,
    operation: DatabaseUpdateOperation = jso()
): Promise<JournalEntryPage?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateJournalEntryPage(callback: PreCreateDocumentCallback<JournalEntryPage, O>) =
    on("preCreateJournalEntryPage", callback)

fun <O> HooksEventListener.onPreUpdateJournalEntryPage(callback: PreUpdateDocumentCallback<JournalEntryPage, O>): Unit =
    on("preUpdateJournalEntryPage", callback)

fun <O> HooksEventListener.onPreDeleteJournalEntryPage(callback: PreDeleteDocumentCallback<JournalEntryPage, O>) =
    on("preDeleteJournalEntryPage", callback)

fun <O> HooksEventListener.onCreateJournalEntryPage(callback: CreateDocumentCallback<JournalEntryPage, O>) =
    on("createJournalEntryPage", callback)

fun <O> HooksEventListener.onUpdateJournalEntryPage(callback: UpdateDocumentCallback<JournalEntryPage, O>) =
    on("updateJournalEntryPage", callback)

fun <O> HooksEventListener.onDeleteJournalEntryPage(callback: DeleteDocumentCallback<JournalEntryPage, O>) =
    on("deleteJournalEntryPage", callback)