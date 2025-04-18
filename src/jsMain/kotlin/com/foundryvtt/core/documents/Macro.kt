@file:JsQualifier("foundry.documents")
package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import kotlin.js.Promise


external class Macro : ClientDocument {
    companion object : DocumentStatic<Macro>

    override fun delete(operation: DatabaseDeleteOperation): Promise<Macro>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<Macro?>

    val isAuthor: Boolean
    val canExecute: Boolean
    val thumbnail: String

    var _id: String
    var name: String
    var type: String
    var author: User
    var img: String
    var scope: String
    var command: String
    var folder: Folder?
    var sort: Int
    var ownership: Ownership

    fun canExecute(user: User): Boolean
    fun execute(scope: MacroScope = definedExternally): ChatMessage
}

