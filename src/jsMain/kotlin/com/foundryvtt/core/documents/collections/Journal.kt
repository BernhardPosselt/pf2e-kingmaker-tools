@file:JsQualifier("foundry.documents.collections")
package com.foundryvtt.core.documents.collections

import com.foundryvtt.core.documents.JournalEntry
import com.foundryvtt.core.documents.JournalEntryPage
import com.foundryvtt.core.documents.abstract.WorldCollection
import kotlin.js.Promise

external class Journal : WorldCollection<JournalEntry> {
    companion object : WorldCollectionStatic<JournalEntry> {
        fun showDialog(document: JournalEntry): Promise<JournalEntry?>
        fun showDialog(document: JournalEntryPage): Promise<JournalEntryPage?>
        fun show(document: JournalEntry, options: JournalShowOptions = definedExternally): Promise<JournalEntry?>
        fun show(
            document: JournalEntryPage,
            options: JournalShowOptions = definedExternally
        ): Promise<JournalEntryPage?>

        fun showImage(src: String, options: ShowImageOptions = definedExternally)
    }
}