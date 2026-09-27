package com.foundryvtt.core.applications.ux.TextEditor

import com.foundryvtt.core.documents.ClientDocument
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface EnrichmentOptions {
    val secrets: Boolean?
    val documents: Boolean?
    val links: Boolean?
    val rolls: Boolean?
    val embeds: Boolean?
    val rollData: Any?
    val relativeTo: ClientDocument?
}