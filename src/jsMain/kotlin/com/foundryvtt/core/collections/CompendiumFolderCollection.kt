package com.foundryvtt.core.collections

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.Document
import com.foundryvtt.core.documents.Folder

open external class CompendiumFolderCollection(
    pack: Document,
    data: Array<AnyObject>
) : DocumentCollection<Folder> {
    val pack: Document
}