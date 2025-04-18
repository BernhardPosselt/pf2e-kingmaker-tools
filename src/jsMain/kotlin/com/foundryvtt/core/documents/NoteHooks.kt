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
fun NoteDocument.update(data: NoteDocument, operation: DatabaseUpdateOperation = jso()): Promise<NoteDocument?> =
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