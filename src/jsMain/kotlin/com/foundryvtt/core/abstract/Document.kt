@file:JsQualifier("foundry.abstract")

package com.foundryvtt.core.abstract

import com.foundryvtt.core.AnyObject
import kotlin.js.Promise

abstract external class Document(
    data: AnyObject = definedExternally,
    options: DocumentConstructionContext = definedExternally
) : DataModel {
    @OptIn(ExperimentalStdlibApi::class)
    @JsExternalInheritorsOnly
    open class DocumentStatic<D : Document> {
        fun create(data: Any, operation: DatabaseCreateOperation = definedExternally): Promise<D>

        fun create(data: D, operation: DatabaseCreateOperation = definedExternally): Promise<D>

        fun createDocuments(
            data: Array<AnyObject>,
            operation: DatabaseCreateOperation = definedExternally
        ): Promise<Array<D>>

        fun createDocuments(
            data: Array<D>,
            operation: DatabaseCreateOperation = definedExternally
        ): Promise<Array<D>>

        fun updateDocuments(
            data: Array<AnyObject>,
            operation: DatabaseUpdateOperation = definedExternally
        ): Promise<Array<D>>

        fun updateDocuments(
            data: Array<D>,
            operation: DatabaseUpdateOperation = definedExternally
        ): Promise<Array<D>>

        fun deleteDocuments(
            data: Array<D>,
            operation: DatabaseDeleteOperation = definedExternally
        ): Promise<Array<D>>

        fun deleteDocuments(
            data: Array<AnyObject>,
            operation: DatabaseDeleteOperation = definedExternally
        ): Promise<Array<D>>

        fun get(id: String, operation: DatabaseGetOperation = definedExternally): Promise<D?>
    }

    companion object : DocumentStatic<Document>

    val id: String?
    val uuid: String
    val isEmbedded: Boolean

    open fun update(
        data: AnyObject,
        operation: DatabaseUpdateOperation = definedExternally
    ): Promise<Document?>

    open fun delete(operation: DatabaseDeleteOperation = definedExternally): Promise<Document>

    open fun <T : Document> createEmbeddedDocuments(
        name: String,
        data: Array<AnyObject>? = definedExternally,
        operation: DatabaseCreateOperation = definedExternally
    ): Promise<Array<T>>

    open fun <T : Document> createEmbeddedDocuments(
        name: String,
        data: Array<T>? = definedExternally,
        operation: DatabaseCreateOperation = definedExternally
    ): Promise<Array<T>>

    open fun <T : Document> updateEmbeddedDocuments(
        name: String,
        data: Array<AnyObject>? = definedExternally,
        operation: DatabaseUpdateOperation = definedExternally
    ): Promise<Array<T>>

    open fun <T : Document> updateEmbeddedDocuments(
        name: String,
        data: Array<T>? = definedExternally,
        operation: DatabaseUpdateOperation = definedExternally
    ): Promise<Array<T>>

    open fun <T : Document> deleteEmbeddedDocuments(
        name: String,
        ids: Array<String>,
        operation: DatabaseDeleteOperation = definedExternally
    ): Promise<Array<T>>

    open fun <T : Document> getEmbeddedDocument(
        name: String,
        id: String,
        operation: DatabaseGetOperation = definedExternally
    ): Promise<T>

    open fun getFlag(scope: String, key: String): Any?
    open fun <T> setFlag(scope: String, key: String, value: T): Promise<T>
    open fun unsetFlag(scope: String, key: String): Promise<Any?>
}

