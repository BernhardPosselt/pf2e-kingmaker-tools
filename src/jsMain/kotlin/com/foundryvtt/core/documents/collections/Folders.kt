@file:JsQualifier("foundry.documents.collections")
package com.foundryvtt.core.documents.collections

import com.foundryvtt.core.documents.Folder
import com.foundryvtt.core.documents.abstract.WorldCollection

external class Folders : WorldCollection<Folder> {
    companion object : WorldCollectionStatic<Folder>
}