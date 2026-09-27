@file:JsQualifier("foundry.documents.collections")
package com.foundryvtt.core.documents.collections

import com.foundryvtt.core.documents.Macro
import com.foundryvtt.core.documents.abstract.WorldCollection

external class Macros : WorldCollection<Macro> {
    companion object : WorldCollectionStatic<Macro>
}