package com.foundryvtt.pf2e.actions

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RollNoteSource {
    val selector: String?
    val title: String?
    val text: String

    //    val predicate: RawPredicate
    val outcome: String?
    val visibility: UserVisibility?
}