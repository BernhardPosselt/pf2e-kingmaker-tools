package com.foundryvtt.core.documents

import com.foundryvtt.core.*
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import js.objects.jso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface TableMessageOptions {
    val roll: Roll?
    val messageData: AnyObject?
    val messageOptions: AnyObject?
}

@JsPlainObject
external interface DrawOptions {
    val roll: Roll?
    val recursive: Boolean?
    val results: Array<TableResult>?
    val displayChat: Boolean?
    val rollMode: String?
}

@JsPlainObject
external interface RollTableDraw {
    val roll: Roll
    val results: Array<TableResult>
}

@JsPlainObject
external interface RollTableRollOptions {
    val roll: Roll
    val recursive: Boolean?
    val _depth: Int?
}

@JsName("CONFIG.RollTable.documentClass")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
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

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun RollTable.update(data: RollTable, operation: DatabaseUpdateOperation = jso()): Promise<RollTable?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateRollTable(callback: PreCreateDocumentCallback<RollTable, O>) =
    on("preCreateRollTable", callback)

fun <O> HooksEventListener.onPreUpdateRollTable(callback: PreUpdateDocumentCallback<RollTable, O>): Unit =
    on("preUpdateRollTable", callback)

fun <O> HooksEventListener.onPreDeleteRollTable(callback: PreDeleteDocumentCallback<RollTable, O>) =
    on("preDeleteRollTable", callback)

fun <O> HooksEventListener.onCreateRollTable(callback: CreateDocumentCallback<RollTable, O>) =
    on("createRollTable", callback)

fun <O> HooksEventListener.onUpdateRollTable(callback: UpdateDocumentCallback<RollTable, O>) =
    on("updateRollTable", callback)

fun <O> HooksEventListener.onDeleteRollTable(callback: DeleteDocumentCallback<RollTable, O>) =
    on("deleteRollTable", callback)