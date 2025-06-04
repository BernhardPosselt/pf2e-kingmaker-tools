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
fun JournalEntryPage.update(
    data: JournalEntryPage,
    operation: DatabaseUpdateOperation = unsafeJso()
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