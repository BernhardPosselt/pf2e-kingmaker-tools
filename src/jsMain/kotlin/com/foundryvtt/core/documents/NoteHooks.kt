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
fun NoteDocument.update(data: NoteDocument, operation: DatabaseUpdateOperation = unsafeJso()): Promise<NoteDocument?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateNote(callback: PreCreateDocumentCallback<NoteDocument, O>) =
    on("preCreateNote", callback)

fun <O> HooksEventListener.onPreUpdateNote(callback: PreUpdateDocumentCallback<NoteDocument, O>): Unit =
    on("preUpdateNote", callback)

fun <O> HooksEventListener.onPreDeleteNote(callback: PreDeleteDocumentCallback<NoteDocument, O>) =
    on("preDeleteNote", callback)

fun <O> HooksEventListener.onCreateNote(callback: CreateDocumentCallback<NoteDocument, O>) =
    on("createNote", callback)

fun <O> HooksEventListener.onUpdateNote(callback: UpdateDocumentCallback<NoteDocument, O>) =
    on("updateNote", callback)

fun <O> HooksEventListener.onDeleteNote(callback: DeleteDocumentCallback<NoteDocument, O>) =
    on("deleteNote", callback)