package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.kingdom.data.RawNotes
import com.foundryvtt.core.ui.TextEditor
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface NotesContext {
    var rawPublic: String
    var public: String
    var rawGm: String
    var gm: String
}

suspend fun RawNotes.toContext(): NotesContext {
    val pub = TextEditor.enrichHTML(public).await()
    val priv = TextEditor.enrichHTML(gm).await()
    return NotesContext(
        rawPublic = public,
        rawGm = gm,
        public = pub,
        gm = priv,
    )
}