package com.foundryvtt.core.applications.api

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.Document
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface DocumentSheetConfiguration<D : Document> : ApplicationConfiguration {
    val document: D
}

@JsPlainObject
external interface SubmitOptions {
    val updateData: AnyObject?
}
