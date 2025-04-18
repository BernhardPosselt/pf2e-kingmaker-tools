@file:JsQualifier("foundry.documents")
package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.documents.collections.EmbeddedCollection
import kotlin.js.Promise

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
