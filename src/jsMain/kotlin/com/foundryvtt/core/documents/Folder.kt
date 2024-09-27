package com.foundryvtt.core.documents

import com.foundryvtt.core.*
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.abstract.Document
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.applications.api.PromptOptions
import com.foundryvtt.core.collections.CompendiumCollection
import com.foundryvtt.core.collections.DocumentCollection
import js.objects.jso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface ExportCompendiumOptions {
    val updateByName: Boolean?
    val keepId: Boolean?
    val keepFolder: Boolean?
    val folder: String?
}

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("CONFIG.Folder.documentClass")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
external class Folder(
    data: AnyObject = definedExternally,
    options: DocumentConstructionContext = definedExternally
) : ClientDocument {
    companion object : DocumentStatic<Folder>

    override fun delete(operation: DatabaseDeleteOperation): Promise<Folder>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<Folder?>

    var _id: String
    val name: String
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

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun Folder.update(data: Folder, operation: DatabaseUpdateOperation = jso()): Promise<Folder?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateFolder(callback: PreCreateDocumentCallback<Folder, O>) =
    on("preCreateFolder", callback)

fun <O> HooksEventListener.onPreUpdateFolder(callback: PreUpdateDocumentCallback<Folder, O>): Unit =
    on("preUpdateFolder", callback)

fun <O> HooksEventListener.onPreDeleteFolder(callback: PreDeleteDocumentCallback<Folder, O>) =
    on("preDeleteFolder", callback)

fun <O> HooksEventListener.onCreateFolder(callback: CreateDocumentCallback<Folder, O>) =
    on("createFolder", callback)

fun <O> HooksEventListener.onUpdateFolder(callback: UpdateDocumentCallback<Folder, O>) =
    on("updateFolder", callback)

fun <O> HooksEventListener.onDeleteFolder(callback: DeleteDocumentCallback<Folder, O>) =
    on("deleteFolder", callback)