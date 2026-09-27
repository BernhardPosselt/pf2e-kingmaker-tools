@file:JsQualifier("foundry.documents.collections")
package com.foundryvtt.core.documents.collections

import com.foundryvtt.core.documents.Item
import com.foundryvtt.core.documents.abstract.WorldCollection

external class Items : WorldCollection<Item> {
    companion object : WorldCollectionStatic<Item>
}