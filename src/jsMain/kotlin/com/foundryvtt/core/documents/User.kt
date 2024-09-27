package com.foundryvtt.core.documents

import com.foundryvtt.core.*
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import js.collections.JsSet
import js.objects.jso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface AssignHotbarMacroOptions {
    val fromSlot: Int?
}

@JsPlainObject
external interface BroadcastActivityOptions {
    val volatile: Boolean?
}

@JsPlainObject
external interface HasRoleOptions {
    val exact: Boolean?
}

@JsPlainObject
external interface HotbarMacros {
    val slot: Int
    val macro: Macro?
}

@JsPlainObject
external interface Permissions {
    val create: Function<Boolean>
    val update: Function<Boolean>
    val delete: Function<Boolean>
}

@JsPlainObject
external interface CursorData {
    val x: Int
    val y: Int
}

@JsPlainObject
external interface PingData {
    val pull: Boolean?
    val style: String
    val scene: String
    val zoom: Int
}

@JsPlainObject
external interface ActivityData {
    val sceneId: String?
    val cursor: CursorData
    val ruler: dynamic
    val targets: Array<String>
    val active: Boolean
    val ping: PingData
    val av: AVSettingsData
}

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("CONFIG.User.documentClass")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
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

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun User.update(data: User, operation: DatabaseUpdateOperation = jso()): Promise<User?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateUser(callback: PreCreateDocumentCallback<User, O>) =
    on("preCreateUser", callback)

fun <O> HooksEventListener.onPreUpdateUser(callback: PreUpdateDocumentCallback<User, O>): Unit =
    on("preUpdateUser", callback)

fun <O> HooksEventListener.onPreDeleteUser(callback: PreDeleteDocumentCallback<User, O>) =
    on("preDeleteUser", callback)

fun <O> HooksEventListener.onCreateUser(callback: CreateDocumentCallback<User, O>) =
    on("createUser", callback)

fun <O> HooksEventListener.onUpdateUser(callback: UpdateDocumentCallback<User, O>) =
    on("updateUser", callback)

fun <O> HooksEventListener.onDeleteUser(callback: DeleteDocumentCallback<User, O>) =
    on("deleteUser", callback)