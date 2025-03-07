package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.kingdom.data.RawNotes
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface NotesContext {
    var public: String
    var gm: String
}

fun RawNotes.toContext() =
    NotesContext(
        public = public,
        gm = gm,
    )