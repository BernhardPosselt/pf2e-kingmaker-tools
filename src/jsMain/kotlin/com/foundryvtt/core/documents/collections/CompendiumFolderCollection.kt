@file:JsQualifier("foundry.documents.collections")
package com.foundryvtt.core.documents.collections

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.Document
import com.foundryvtt.core.documents.Folder
import com.foundryvtt.core.documents.abstract.DocumentCollection

open external class CompendiumFolderCollection(
    pack: Document,
    data: Array<AnyObject>
) : DocumentCollection<Folder> {
    val pack: Document
}