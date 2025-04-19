@file:JsQualifier("foundry.documents")
package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import kotlin.js.Promise

external class RollTable : ClientDocument {
    companion object : DocumentStatic<RollTable>

    override fun delete(operation: DatabaseDeleteOperation): Promise<RollTable>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<RollTable?>

    var _id: String
    var name: String
    var img: String
    var description: String
    var results: Array<TableResult>
    var formula: String
    var replacement: Boolean
    var displayRoll: Boolean
    var folder: Folder
    var sort: Int
    var onwership: Ownership

    val thumbnail: String
    fun toMessage(data: Array<TableResult>, options: TableMessageOptions = definedExternally): Promise<ChatMessage>
    fun draw(options: DrawOptions = definedExternally): Promise<RollTableDraw>
    fun drawMany(number: Int, options: DrawOptions = definedExternally): Promise<RollTableDraw>
    fun normalize(): Promise<RollTable>
    fun resetResults(): Promise<RollTable>
    fun roll(options: RollTableRollOptions): Promise<RollTableDraw>
    fun getResultsForRoll(value: Int): Array<TableResult>
}

