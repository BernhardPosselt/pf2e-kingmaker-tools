package com.foundryvtt.core.documents

import com.foundryvtt.core.*
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.abstract.DocumentConstructionContext
import js.objects.ReadonlyRecord
import js.objects.jso
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLHeadingElement
import kotlin.js.Promise

@JsPlainObject
external interface JournalEntryPageHeading {
    val number: Int
    val text: String
    val slug: String
    val element: HTMLHeadingElement
    val children: Array<String>
    val order: Int
}


@JsPlainObject
external interface BuildTOCOptions {
    val includeElement: Boolean
}

@JsPlainObject
external interface JournalTextData {
    var content: String
    var markdown: String
    var format: Int
}

@JsPlainObject
external interface JournalTitleData {
    var show: Boolean
    var level: Int
}

@JsPlainObject
external interface JournalVideoData {
    var controls: Boolean
    var loop: Boolean
    var autoplay: Boolean
    var voluem: Double
    var timestamp: Int
    var width: Int
    var height: Int
}

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("CONFIG.JournalEntryPage.documentClass")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
external class JournalEntryPage(
    data: AnyObject = definedExternally,
    options: DocumentConstructionContext = definedExternally
) : ClientDocument {
    companion object : DocumentStatic<JournalEntryPage> {
        fun slugifyHeading(heading: String): String
        fun slugifyHeading(heading: HTMLHeadingElement): String
        fun buildTOC(
            html: Array<HTMLElement>,
            options: BuildTOCOptions = definedExternally
        ): ReadonlyRecord<String, JournalEntryPageHeading>
    }

    override fun delete(operation: DatabaseDeleteOperation): Promise<JournalEntryPage>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<JournalEntryPage?>

    val toc: ReadonlyRecord<String, JournalEntryPageHeading>
    override val parent: JournalEntry?

    var _id: String
    var name: String
    var type: String
    var title: JournalTitleData
    var image: String
    var text: JournalTextData
    var video: JournalVideoData
    var src: String
    var sort: Int
    var ownership: Ownership
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun JournalEntryPage.update(
    data: JournalEntryPage,
    operation: DatabaseUpdateOperation = jso()
): Promise<JournalEntryPage?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateJournalEntryPage(callback: PreCreateDocumentCallback<JournalEntryPage, O>) =
    on("preCreateJournalEntryPage", callback)

fun <O> HooksEventListener.onPreUpdateJournalEntryPage(callback: PreUpdateDocumentCallback<JournalEntryPage, O>): Unit =
    on("preUpdateJournalEntryPage", callback)

fun <O> HooksEventListener.onPreDeleteJournalEntryPage(callback: PreDeleteDocumentCallback<JournalEntryPage, O>) =
    on("preDeleteJournalEntryPage", callback)

fun <O> HooksEventListener.onCreateJournalEntryPage(callback: CreateDocumentCallback<JournalEntryPage, O>) =
    on("createJournalEntryPage", callback)

fun <O> HooksEventListener.onUpdateJournalEntryPage(callback: UpdateDocumentCallback<JournalEntryPage, O>) =
    on("updateJournalEntryPage", callback)

fun <O> HooksEventListener.onDeleteJournalEntryPage(callback: DeleteDocumentCallback<JournalEntryPage, O>) =
    on("deleteJournalEntryPage", callback)