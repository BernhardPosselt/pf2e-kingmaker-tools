package com.foundryvtt.core.collections

import com.foundryvtt.core.documents.Item

external class Items : WorldCollection<Item> {
    companion object : WorldCollectionStatic<Item>
}