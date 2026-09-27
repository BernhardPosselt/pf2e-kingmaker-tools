@file:JsQualifier("foundry.documents.collections")
package com.foundryvtt.core.documents.collections

import com.foundryvtt.core.documents.RollTable
import com.foundryvtt.core.documents.abstract.WorldCollection

external class RollTables : WorldCollection<RollTable> {
    companion object : WorldCollectionStatic<RollTable>
}