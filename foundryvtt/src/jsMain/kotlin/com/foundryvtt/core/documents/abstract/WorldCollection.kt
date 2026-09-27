@file:JsQualifier("foundry.documents.abstract")
package com.foundryvtt.core.documents.abstract

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseGetOperation
import com.foundryvtt.core.abstract.Document
import com.foundryvtt.core.applications.api.ApplicationV2
import com.foundryvtt.core.applications.api.DocumentSheetV2
import com.foundryvtt.core.documents.DocumentSheet
import com.foundryvtt.core.documents.RegisterSheetConfig
import com.foundryvtt.core.documents.collections.CompendiumCollection
import com.foundryvtt.core.documents.collections.Folders
import com.foundryvtt.core.utils.Collection
import kotlin.js.Promise


abstract external class WorldCollection<T>(
    data: Array<AnyObject>
) : Collection<T> {
    open class WorldCollectionStatic<D : Document> {
        fun registerSheet(scope: String, sheet: JsClass<out DocumentSheetV2<D>>, config: RegisterSheetConfig)
        fun unregisterSheet(application: ApplicationV2)
        val registeredSheets: Array<DocumentSheet>
    }

    val folders: Collection<Folders>
    val instance: WorldCollection<T>?

    fun importFromCompendium(
        pack: CompendiumCollection<Document>,
        id: String,
        updateData: AnyObject = definedExternally,
        options: DatabaseGetOperation = definedExternally
    ): Promise<Document>

    fun fromCompendium(document: Document, options: FromCompendiumOptions = definedExternally): Promise<AnyObject>
}