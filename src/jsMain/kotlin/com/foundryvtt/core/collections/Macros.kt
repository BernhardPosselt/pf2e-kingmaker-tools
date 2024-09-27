package com.foundryvtt.core.collections

import com.foundryvtt.core.documents.Macro

external class Macros : WorldCollection<Macro> {
    companion object : WorldCollectionStatic<Macro>
}