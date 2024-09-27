package com.foundryvtt.core.ui

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.documents.ClientDocument
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import kotlin.js.Promise

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

@JsPlainObject
external interface TruncateOptions {
    val maxLength: Int?
    val splitWords: Boolean?
    val suffix: String?
}


external class TextEditor {
    companion object {
        fun create(
            options: AnyObject = definedExternally,
            content: String = definedExternally
        ): Promise<Any> // PromiseMirror or TinyMCE instance

        fun decodeHTML(html: String): Promise<String>
        fun enrichHTML(content: String, options: EnrichmentOptions = definedExternally): Promise<String>
        fun previewHTML(content: String, length: Int = definedExternally): String
        fun truncateHTML(html: HTMLElement): HTMLElement
        fun truncateText(text: String, options: TruncateOptions = definedExternally): String
    }
}