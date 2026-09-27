package com.foundryvtt.core.documents.collections

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ImportAllOptions {
    val folderId: String?
    val folderName: String?
}