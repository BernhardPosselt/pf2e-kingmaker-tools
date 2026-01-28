package at.posselt.pfrpg2e.utils

import js.objects.Object
import js.objects.Record
import js.reflect.Reflect


@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class DocumentUpdateDsl

@DocumentUpdateDsl
open class PropertyUpdateBuilder<T>(
    val basePath: String,
    val updates: Record<String, Any?>,
    val propertyName: String
) {
    fun delete() {
        Object.keys(updates)
            .filter { it.startsWith("$basePath.$propertyName.") || it == "$basePath.$propertyName" }
            .forEach { Reflect.deleteProperty(updates, it) }
        updates["$basePath.-=$propertyName"] = null
    }

    fun set(value: T) {
        updates["$basePath.$propertyName"] = value
    }
}

@DocumentUpdateDsl
open class RecordPropertyUpdateBuilder<T>(
    basePath: String,
    updates: Record<String, Any?>,
    propertyName: String
) : PropertyUpdateBuilder<T>(basePath, updates, propertyName) {
    fun deleteEntry(key: String) {
        Reflect.deleteProperty(updates, "$basePath.$propertyName.$key")
        updates["$basePath.$propertyName-=$key"] = null
    }

    fun deleteEntries(keys: Set<String>) = keys.forEach(::deleteEntry)

    operator fun set(key: String, value: T) {
        updates["$basePath.$propertyName.$key"] = value
    }
}
