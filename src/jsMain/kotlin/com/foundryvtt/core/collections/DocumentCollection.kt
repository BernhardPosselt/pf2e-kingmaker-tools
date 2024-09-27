package com.foundryvtt.core.collections

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.Document
import com.foundryvtt.core.applications.api.ApplicationV2
import com.foundryvtt.core.utils.Collection
import js.collections.JsSet
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface FieldFilter

@JsPlainObject
external interface SearchOptions {
    val query: String?
    val filters: Array<FieldFilter>?
    val exclude: Array<String>
}

open external class DocumentCollection<T : Document>(
    data: Array<AnyObject>
) : Collection<T> {
    val _source: Array<AnyObject>
    val apps: Array<ApplicationV2>
    val documentName: String
    val documentClass: JsClass<Document>
    val invalidDocumentIds: JsSet<String>
    val name: String

    fun search(options: SearchOptions = definedExternally): Array<String>
}