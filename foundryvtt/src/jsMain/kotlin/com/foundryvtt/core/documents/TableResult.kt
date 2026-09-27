@file:JsQualifier("foundry.documents")
package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import kotlin.js.Promise

external class TableResult : ClientDocument {
    companion object : DocumentStatic<TableResult>

    override fun delete(operation: DatabaseDeleteOperation): Promise<TableResult>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<TableResult?>

    val icon: String
    fun getChatText(): String

    var _id: String
    var type: String
    var text: String
    var img: String
    var documentCollection: String
    var documentId: String
    var weight: Int
    var range: Array<Int>
    var drawn: Boolean
}

