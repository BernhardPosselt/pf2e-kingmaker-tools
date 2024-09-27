package com.foundryvtt.core.documents

import com.foundryvtt.core.*
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.collections.EmbeddedCollection
import js.objects.jso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface PanToNoteOptions {
    val scale: Double?
    val duration: Int?
}

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("CONFIG.JournalEntry.documentClass")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
external class JournalEntry(
    data: AnyObject = definedExternally,
    options: DocumentConstructionContext = definedExternally
) : ClientDocument {
    companion object : DocumentStatic<JournalEntry> {
        fun show(force: Boolean = definedExternally): Promise<JournalEntry>
    }

    override fun delete(operation: DatabaseDeleteOperation): Promise<JournalEntry>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<JournalEntry?>

    fun panToNote(options: PanToNoteOptions): Promise<Unit>

    var _id: String
    var name: String
    var pages: EmbeddedCollection<JournalEntryPage>
    var folder: Folder?
    var sort: Int
    var ownership: Ownership
}

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