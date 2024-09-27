package com.foundryvtt.core.collections

import com.foundryvtt.core.documents.JournalEntry
import com.foundryvtt.core.documents.JournalEntryPage
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface JournalShowOptions {
    val users: Array<String>?
    val force: Boolean?
}

@JsPlainObject
external interface ShowImageOptions {
    val users: Array<String>?
}

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