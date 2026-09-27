@file:JsQualifier("foundry.documents")
package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.abstract.DocumentConstructionContext
import js.objects.ReadonlyRecord
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLHeadingElement
import kotlin.js.Promise


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
