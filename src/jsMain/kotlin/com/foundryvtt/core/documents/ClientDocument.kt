package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.Document
import com.foundryvtt.core.applications.api.ApplicationV2
import js.objects.Record
import kotlin.js.Promise

open external class ClientDocument : Document {
    companion object {
        fun fromDropData(data: AnyObject, options: AnyObject = definedExternally): Promise<Document>
    }

    val apps: Record<String, ApplicationV2>
    val isOwner: Boolean
    val hasPlayerOwner: Boolean
    val limited: Boolean
    val link: String
    val permission: Ownership
    val sheet: ApplicationV2?
    val visible: Boolean
    fun render(force: Boolean = definedExternally, context: AnyObject = definedExternally)
    fun getRelativeUUID(document: ClientDocument): String
    fun toDragData(): AnyObject
}