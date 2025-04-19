@file:JsQualifier("foundry.documents")
package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.abstract.Document
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.applications.api.PromptOptions
import com.foundryvtt.core.documents.abstract.DocumentCollection
import com.foundryvtt.core.documents.collections.CompendiumCollection
import kotlin.js.Promise

external class Folder(
    data: AnyObject = definedExternally,
    options: DocumentConstructionContext = definedExternally
) : ClientDocument {
    companion object : DocumentStatic<Folder>

    override fun delete(operation: DatabaseDeleteOperation): Promise<Folder>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<Folder?>

    var _id: String
    var name: String
    val description: String
    val folder: Folder
    val sorting: String
    val sort: Int
    val depth: Int
    val children: Array<Folder>
    val displayed: Boolean
    var contents: Array<Any>
    val expanded: Boolean
    val ancestors: Array<Folder>
    fun exportToCompendium(
        pack: DocumentCollection<Document>,
        options: ExportCompendiumOptions = definedExternally
    ): Promise<CompendiumCollection<Folder>>

    fun exportDialog(pack: String, options: PromptOptions = definedExternally): Promise<Unit>
    fun getSubFolders(recursive: Boolean = definedExternally): Array<Folder>
    fun getParentFolders(): Array<Folder>
}
