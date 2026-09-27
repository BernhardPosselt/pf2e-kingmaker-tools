@file:JsQualifier("foundry.documents.collections")
package com.foundryvtt.core.documents.collections

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.Document
import com.foundryvtt.core.applications.api.ApplicationV2
import com.foundryvtt.core.applications.api.ConfirmOptions
import com.foundryvtt.core.documents.User
import com.foundryvtt.core.documents.abstract.DocumentCollection
import com.foundryvtt.core.utils.Collection
import js.collections.JsSet
import js.objects.Record
import kotlin.js.Promise


open external class CompendiumCollectionStatic<T : Document> {
    fun createCompendium(metadata: AnyObject, options: AnyObject = definedExternally): Promise<CompendiumCollection<T>>
}

open external class CompendiumCollection<T : Document>(
    metadata: AnyObject
) : DocumentCollection<T> {
    companion object : CompendiumCollectionStatic<Document>

    val metadata: AnyObject
    val index: Collection<T>
    val collection: String
    val banner: String?
    val applicationClass: JsClass<ApplicationV2>
    val folders: CompendiumFolderCollection
    val maxFolderDepth: Int
    val folder: Folders
    val sort: Int
    val locked: Boolean
    val ownership: Int
    val visible: Boolean
    val title: String
    val indexedFields: JsSet<String>
    val indexed: Boolean


    fun setFolder(folder: Folders?): Promise<Unit>
    fun getIndex(options: GetIndexOptions = definedExternally): Promise<Collection<T>>
    fun getDocument(id: String): Promise<Document?>
    fun getDocuments(query: AnyObject = definedExternally): Promise<Array<Document>>
    fun getUserLevel(user: User): Int
    fun importDocument(document: Document): Promise<Document>
    fun importFolder(folder: Folders, options: ImportFolderOptions): Promise<Unit>
    fun importAll(options: ImportAllOptions = definedExternally): Promise<Document>
    fun importDialog(options: ConfirmOptions = definedExternally): Promise<Any?>
    fun indexDocument(document: Document)
    fun configureOwnershipDialog(): Promise<Record<String, String>>
    fun getUuid(id: String): String
    fun configure(configuration: AnyObject = definedExternally): Promise<Unit>
    fun deleteCompendium(): Promise<CompendiumCollection<T>>
    fun duplicateCompendium(options: DuplicateCompendiumOptions = definedExternally): Promise<CompendiumCollection<T>>
    fun migrate(): Promise<CompendiumCollection<T>>
}