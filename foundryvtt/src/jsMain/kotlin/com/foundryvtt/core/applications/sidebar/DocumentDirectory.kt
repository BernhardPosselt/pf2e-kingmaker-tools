@file:JsQualifier("foundry.applications.sidebar.tabs")
package com.foundryvtt.core.applications.sidebar

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.Document
import com.foundryvtt.core.documents.Folder
import com.foundryvtt.core.documents.abstract.WorldCollection
import js.collections.JsSet
import org.w3c.dom.HTMLElement

abstract external class DocumentDirectory<T : Document> {
    companion object {
        var documentName: String
        var entryPartial: String
    }

    val entryType: String
    val maxFolderDepth: Int
    val title: String
    val id: String
    val tabName: String
    val collection: WorldCollection<T>
    val canCreateEntry: Boolean
    val canCreateFolder: Boolean

    var document: Array<T>
    var folders: Array<Folder>

    open fun initialize()
    protected open fun _matchSearchFolders(query: Regex, includeFolder: (folder: Folder, config: Boolean?) -> Unit)
    protected open fun _matchSearchEntries(
        query: Regex,
        includeFolder: (folder: Folder, entryIds: JsSet<String>, folderIds: JsSet<String>, config: Boolean?) -> Unit
    )
    protected open fun _handleDroppedFolder(target: HTMLElement, data: AnyObject)
    // TODO

}