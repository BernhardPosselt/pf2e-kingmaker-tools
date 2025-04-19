package com.foundryvtt.core.documents.abstract

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface FromCompendiumOptions {
    val clearFolder: Boolean?
    val clearSort: Boolean?
    val clearOwnership: Boolean?
    val keepId: Boolean?
}