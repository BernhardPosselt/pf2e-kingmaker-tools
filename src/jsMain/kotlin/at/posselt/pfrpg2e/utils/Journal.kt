package at.posselt.pfrpg2e.utils

import com.foundryvtt.core.applications.api.ApplicationRenderOptions
import com.foundryvtt.core.documents.JournalEntry
import com.foundryvtt.core.documents.JournalEntryPage
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface JournalRenderOptions : ApplicationRenderOptions {
    val pageId: String?
}

suspend fun openJournal(uuid: String) {
    val page = fromUuidTypeSafe<JournalEntryPage>(uuid)
    if (page == null) {
        fromUuidTypeSafe<JournalEntry>(uuid)?.sheet?.render(true)
    } else {
        page.parent?.sheet?.render(
            true, JournalRenderOptions(pageId = page.id)
        )
    }
}