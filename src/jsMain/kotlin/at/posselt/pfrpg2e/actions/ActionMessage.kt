package at.posselt.pfrpg2e.actions

import com.foundryvtt.core.AnyObject
import js.objects.recordOf
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ActionMessage {
    val action: String
    val data: AnyObject
}

fun emptyActionMessage(action: String) = ActionMessage(action = action, data = recordOf())