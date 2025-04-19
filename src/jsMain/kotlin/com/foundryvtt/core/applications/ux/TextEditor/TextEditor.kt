@file:JsQualifier("foundry.applications.ux.TextEditor")

package com.foundryvtt.core.applications.ux.TextEditor

import com.foundryvtt.core.AnyObject
import org.w3c.dom.HTMLElement
import kotlin.js.Promise

@JsName("implementation")
external class TextEditor {
    companion object {
        fun create(
            options: AnyObject = definedExternally,
            content: String = definedExternally
        ): Promise<Any> // PromiseMirror or TinyMCE instance

        fun decodeHTML(html: String): Promise<String>
        fun enrichHTML(content: String, options: EnrichmentOptions? = definedExternally): Promise<String>
        fun previewHTML(content: String, length: Int = definedExternally): String
        fun truncateHTML(html: HTMLElement): HTMLElement
        fun truncateText(text: String, options: TruncateOptions = definedExternally): String
    }
}

