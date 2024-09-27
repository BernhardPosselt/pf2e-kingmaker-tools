package com.foundryvtt.core.collections

import com.foundryvtt.core.documents.User

external class Users : WorldCollection<User> {
    companion object : WorldCollectionStatic<User>

    val players: Array<User>
    val activeGM: User?
}