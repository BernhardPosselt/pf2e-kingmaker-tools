package com.foundryvtt.core.collections

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.Document
import com.foundryvtt.core.documents.Folder
import com.foundryvtt.core.utils.Collection

external class CompendiumPacks : Collection<CompendiumCollection<Document>> {
    val folders: Collection<Folder>
    val tree: AnyObject
    val searchMode: String
    val sortingMode: String
    val maxFolderDepth: Int
    fun toggleSearchMode()
    fun toggleSortingMode()
    fun initializeTree()
}