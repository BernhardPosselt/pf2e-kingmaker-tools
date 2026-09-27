@file:JsQualifier("foundry.documents.collections")
package com.foundryvtt.core.documents.collections

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.documents.Combat
import com.foundryvtt.core.documents.abstract.WorldCollection

external class CombatEncounters : WorldCollection<Combat> {
    companion object : WorldCollectionStatic<Combat>

    val settings: AnyObject
    val combats: Array<Combat>
    val active: Combat?
    val viewed: Combat?
}