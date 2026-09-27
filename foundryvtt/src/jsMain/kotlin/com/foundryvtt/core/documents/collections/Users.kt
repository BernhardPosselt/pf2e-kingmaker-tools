@file:JsQualifier("foundry.documents.collections")
package com.foundryvtt.core.documents.collections

import com.foundryvtt.core.documents.User
import com.foundryvtt.core.documents.abstract.WorldCollection

external class Users : WorldCollection<User> {
    companion object : WorldCollectionStatic<User>

    val players: Array<User>
    val activeGM: User?
}