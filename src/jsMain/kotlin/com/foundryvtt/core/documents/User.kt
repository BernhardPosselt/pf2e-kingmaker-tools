@file:JsQualifier("foundry.documents")
package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import js.collections.JsSet
import kotlin.js.Promise

external class User : ClientDocument {
    companion object : DocumentStatic<User>

    override fun delete(operation: DatabaseDeleteOperation): Promise<User>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<User?>

    val active: Boolean
    val targets: JsSet<TokenDocument>
    val viewedScene: String?
    val isTrusted: Boolean
    val isSelf: Boolean
    val isGM: Boolean
    val isBanned: Boolean

    var _id: String
    val name: String
    var role: Int
    var password: String
    var passwordSalt: String
    var avatar: String
    var character: Actor?
    var color: String
    var pronouns: String
    var hotbar: HotbarMacros
    var permissions: Permissions


    fun assignHotbarMacro(
        macro: Macro?,
        slot: Int,
        options: AssignHotbarMacroOptions = definedExternally
    ): Promise<User>

    fun broadcastActivity(activityData: ActivityData, options: BroadcastActivityOptions)
    fun assignPermission(permission: String, allowed: Boolean): Promise<User>
    fun getHotbarMacros(page: Int = definedExternally): HotbarMacros
    fun updateTokenTargets(targetIds: Array<String> = definedExternally)
    fun hasPermission(permission: String): Boolean
    fun hasRole(role: Int, options: HasRoleOptions): Boolean
    fun hasRole(role: String, options: HasRoleOptions): Boolean
}
