package com.foundryvtt.core.applications.ux.TextEditor

import kotlinx.coroutines.await

suspend fun enrichHtml(content: String, options: EnrichmentOptions? = undefined) =
    TextEditor.enrichHTML(content, options).await()