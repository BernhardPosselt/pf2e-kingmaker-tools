package com.foundryvtt.pf2e.actions

import com.foundryvtt.core.Roll
import com.foundryvtt.core.documents.User

external class CheckRoll : Roll {
    val roller: User
    val type: String
    val degreeOfSuccess: Int
    val isReroll: Boolean
    val isRerollable: Boolean
}