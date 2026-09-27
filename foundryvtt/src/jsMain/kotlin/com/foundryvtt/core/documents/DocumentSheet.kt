package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RegisterSheetConfig {
    val label: String
    val types: Array<String>
    val makeDefault: Boolean?
    val canBeDefault: Boolean?
    val canConfigure: Boolean?
}

open external class DocumentSheet {
    companion object {
        fun registerSheet(
            document: ClientDocument,
            scope: String,
            sheetClass: DocumentSheet,
            options: RegisterSheetConfig
        )
    }

    // TODO
}