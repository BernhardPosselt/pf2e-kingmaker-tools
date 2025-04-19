package com.foundryvtt.core.documents.collections

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ImportFolderOptions {
    val importParents: Boolean?
}