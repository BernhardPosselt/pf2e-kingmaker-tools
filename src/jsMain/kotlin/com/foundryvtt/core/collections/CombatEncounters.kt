package com.foundryvtt.core.collections

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.documents.Combat

external class CombatEncounters : WorldCollection<Combat> {
    companion object : WorldCollectionStatic<Combat>

    val settings: AnyObject
    val combats: Array<Combat>
    val active: Combat?
    val viewed: Combat?
}